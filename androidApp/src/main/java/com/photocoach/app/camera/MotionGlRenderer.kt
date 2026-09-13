package com.photocoach.app.camera

import android.opengl.EGL14 as E
import android.opengl.GLES30 as G
import android.opengl.EGLConfig
import android.opengl.EGLSurface
import android.opengl.EGLExt
import android.view.Surface
import java.nio.ByteBuffer

/** One owner thread/context. No camera OES texture is ever handed to another GL owner. */
internal class MotionGlRenderer : AutoCloseable {
    private val display=CameraEglDisplay.get()
    private var config:EGLConfig?=null
    private var context=E.EGL_NO_CONTEXT
    private var scratch=E.EGL_NO_SURFACE
    private val programs=mutableMapOf<Boolean,Int>()
    private var disposed=false
    init {
        try {
            val configs=arrayOfNulls<EGLConfig>(1)
            check(E.eglChooseConfig(display,intArrayOf(E.EGL_RENDERABLE_TYPE,0x40,
                E.EGL_SURFACE_TYPE,E.EGL_WINDOW_BIT or E.EGL_PBUFFER_BIT,
                E.EGL_RED_SIZE,8,E.EGL_GREEN_SIZE,8,E.EGL_BLUE_SIZE,8,E.EGL_ALPHA_SIZE,8,
                0x3142,1,E.EGL_NONE),0,configs,0,1,IntArray(1),0))
            config=requireNotNull(configs[0])
            context=E.eglCreateContext(display,config,E.EGL_NO_CONTEXT,intArrayOf(E.EGL_CONTEXT_CLIENT_VERSION,3,E.EGL_NONE),0)
            check(context!=E.EGL_NO_CONTEXT)
            scratch=pbuffer(1,1);current()
        } catch(error:Throwable) {close();throw error}
    }
    fun pbuffer(width:Int,height:Int):EGLSurface = E.eglCreatePbufferSurface(display,config,
        intArrayOf(E.EGL_WIDTH,width,E.EGL_HEIGHT,height,E.EGL_NONE),0).also {check(it!=E.EGL_NO_SURFACE)}
    fun window(surface:Surface):EGLSurface=E.eglCreateWindowSurface(display,config,surface,intArrayOf(E.EGL_NONE),0)
        .also {check(it!=E.EGL_NO_SURFACE)}
    fun current(surface:EGLSurface=scratch) {check(E.eglMakeCurrent(display,surface,surface,context))}
    fun destroy(surface:EGLSurface) {current();check(E.eglDestroySurface(display,surface))}
    fun texture(external:Boolean):Int {
        val ids=IntArray(1);G.glGenTextures(1,ids,0)
        val target=if(external)EXTERNAL else G.GL_TEXTURE_2D
        G.glBindTexture(target,ids[0])
        G.glTexParameteri(target,G.GL_TEXTURE_MIN_FILTER,G.GL_LINEAR)
        G.glTexParameteri(target,G.GL_TEXTURE_MAG_FILTER,G.GL_LINEAR)
        G.glTexParameteri(target,G.GL_TEXTURE_WRAP_S,G.GL_CLAMP_TO_EDGE)
        G.glTexParameteri(target,G.GL_TEXTURE_WRAP_T,G.GL_CLAMP_TO_EDGE)
        checkGl();return ids[0]
    }
    fun deleteTexture(texture:Int) {G.glDeleteTextures(1,intArrayOf(texture),0)}
    fun draw(texture:Int,external:Boolean,transform:FloatArray,surface:EGLSurface,width:Int,height:Int) {
        current(surface)
        val program=programs.getOrPut(external){program(external)}
        G.glBindFramebuffer(G.GL_FRAMEBUFFER,0);G.glViewport(0,0,width,height);G.glUseProgram(program)
        G.glActiveTexture(G.GL_TEXTURE0);G.glBindTexture(if(external)EXTERNAL else G.GL_TEXTURE_2D,texture)
        G.glUniform1i(G.glGetUniformLocation(program,"tex"),0)
        G.glUniformMatrix4fv(G.glGetUniformLocation(program,"tx"),1,false,transform,0)
        G.glDrawArrays(G.GL_TRIANGLES,0,3);checkGl()
    }
    /** glReadPixels completes the copy before the leased bytes become visible to output workers. */
    fun read(width:Int,height:Int,pixels:ByteBuffer) {
        pixels.clear();G.glReadPixels(0,0,width,height,G.GL_RGBA,G.GL_UNSIGNED_BYTE,pixels);checkGl();pixels.rewind()
    }
    fun upload(texture:Int,width:Int,height:Int,pixels:ByteBuffer) {
        G.glBindTexture(G.GL_TEXTURE_2D,texture)
        pixels.rewind();G.glTexImage2D(G.GL_TEXTURE_2D,0,G.GL_RGBA,width,height,0,G.GL_RGBA,G.GL_UNSIGNED_BYTE,pixels);checkGl()
    }
    fun present(surface:EGLSurface,timestampNs:Long) {
        require(timestampNs>=0)
        check(EGLExt.eglPresentationTimeANDROID(display,surface,timestampNs))
        check(E.eglSwapBuffers(display,surface))
    }
    private fun program(external:Boolean):Int {
        fun shader(kind:Int,source:String):Int {
            val shader=G.glCreateShader(kind);G.glShaderSource(shader,source);G.glCompileShader(shader)
            val ok=IntArray(1);G.glGetShaderiv(shader,G.GL_COMPILE_STATUS,ok,0)
            if(ok[0]==0){val message=G.glGetShaderInfoLog(shader);G.glDeleteShader(shader);error(message)}
            return shader
        }
        val vertex=shader(G.GL_VERTEX_SHADER,"""#version 300 es
            uniform mat4 tx; out vec2 uv;
            void main(){vec2 p=vec2(float((gl_VertexID<<1)&2),float(gl_VertexID&2));
                gl_Position=vec4(p*2.0-1.0,0.0,1.0);uv=(tx*vec4(p,0.0,1.0)).xy;}
        """.trimIndent())
        var fragment=0;var program=0
        try {
            fragment=shader(G.GL_FRAGMENT_SHADER,"#version 300 es\n"+
                (if(external)"#extension GL_OES_EGL_image_external_essl3 : require\n" else "")+
                "precision mediump float; in vec2 uv; uniform "+(if(external)"samplerExternalOES" else "sampler2D")+
                " tex; out vec4 color; void main(){color=texture(tex,uv);}")
            program=G.glCreateProgram();G.glAttachShader(program,vertex);G.glAttachShader(program,fragment);G.glLinkProgram(program)
            val ok=IntArray(1);G.glGetProgramiv(program,G.GL_LINK_STATUS,ok,0);check(ok[0]!=0){G.glGetProgramInfoLog(program)}
            return program
        } catch(error:Throwable){if(program!=0)G.glDeleteProgram(program);throw error}
        finally {G.glDeleteShader(vertex);if(fragment!=0)G.glDeleteShader(fragment)}
    }
    override fun close() {
        if(disposed)return
        disposed=true
        try {
            if(context!=E.EGL_NO_CONTEXT && scratch!=E.EGL_NO_SURFACE) {
                current();programs.values.forEach(G::glDeleteProgram);programs.clear()
            }
        } finally {
            E.eglMakeCurrent(display,E.EGL_NO_SURFACE,E.EGL_NO_SURFACE,E.EGL_NO_CONTEXT)
            if(scratch!=E.EGL_NO_SURFACE)E.eglDestroySurface(display,scratch)
            if(context!=E.EGL_NO_CONTEXT)E.eglDestroyContext(display,context)
            E.eglReleaseThread()
        }
    }
    companion object {
        const val EXTERNAL=0x8D65
        fun identity()=FloatArray(16).also {android.opengl.Matrix.setIdentityM(it,0)}
        private fun checkGl(){check(G.glGetError()==G.GL_NO_ERROR){"Live GL operation failed"}}
    }
}
