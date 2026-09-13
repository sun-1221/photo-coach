package com.photocoach.app.beauty

import android.opengl.EGL14 as E
import android.opengl.GLES30 as G
import android.opengl.EGLConfig
import android.opengl.EGLSurface
import android.view.Surface

/** Confined to one GL thread. Half-float FBO support is checked, never assumed. */
internal class BeautyGlRenderer : AutoCloseable {
    private val display = com.photocoach.app.camera.CameraEglDisplay.get()
    private lateinit var config: EGLConfig
    private var context: android.opengl.EGLContext = E.EGL_NO_CONTEXT
    private var scratch: EGLSurface = E.EGL_NO_SURFACE
    private var initialized = false
    private var disposed = false
    private val programs = mutableMapOf<String, Int>()
    private val targets = mutableListOf<Target>()
    private var size = 0 to 0
    init {
        try {
        initialized = true
        val configs = arrayOfNulls<EGLConfig>(1)
        check(E.eglChooseConfig(display, intArrayOf(E.EGL_RENDERABLE_TYPE,0x40,
            E.EGL_SURFACE_TYPE,E.EGL_WINDOW_BIT or E.EGL_PBUFFER_BIT,
            E.EGL_RED_SIZE,8,E.EGL_GREEN_SIZE,8,E.EGL_BLUE_SIZE,8,E.EGL_ALPHA_SIZE,8,E.EGL_NONE),
            0,configs,0,1,IntArray(1),0))
        config = requireNotNull(configs[0])
        context = E.eglCreateContext(display,config,E.EGL_NO_CONTEXT,intArrayOf(E.EGL_CONTEXT_CLIENT_VERSION,3,E.EGL_NONE),0)
        check(context != E.EGL_NO_CONTEXT)
        scratch = pbuffer(1,1)
        makeCurrent(scratch)
        } catch (error: Throwable) {
            releaseEgl()
            throw error
        }
    }
    fun pbuffer(w:Int,h:Int): EGLSurface = E.eglCreatePbufferSurface(display,config,
        intArrayOf(E.EGL_WIDTH,w,E.EGL_HEIGHT,h,E.EGL_NONE),0).also { check(it != E.EGL_NO_SURFACE) }
    fun window(surface: Surface): EGLSurface = E.eglCreateWindowSurface(display,config,surface,intArrayOf(E.EGL_NONE),0)
        .also { check(it != E.EGL_NO_SURFACE) }
    fun destroy(surface:EGLSurface) { makeCurrent(scratch); E.eglDestroySurface(display,surface) }
    fun makeCurrent(surface:EGLSurface=scratch) { check(E.eglMakeCurrent(display,surface,surface,context)) }
    fun swap(surface:EGLSurface) { check(E.eglSwapBuffers(display,surface)) }
    fun texture(external:Boolean=false):Int {
        val id=IntArray(1); G.glGenTextures(1,id,0)
        val type=if(external) EXTERNAL else G.GL_TEXTURE_2D
        G.glBindTexture(type,id[0])
        G.glTexParameteri(type,G.GL_TEXTURE_MIN_FILTER,G.GL_LINEAR)
        G.glTexParameteri(type,G.GL_TEXTURE_MAG_FILTER,G.GL_LINEAR)
        G.glTexParameteri(type,G.GL_TEXTURE_WRAP_S,G.GL_CLAMP_TO_EDGE)
        G.glTexParameteri(type,G.GL_TEXTURE_WRAP_T,G.GL_CLAMP_TO_EDGE)
        try { checkGl(); return id[0] }
        catch (error: Throwable) { deleteTexture(id[0]); throw error }
    }
    fun deleteTexture(id:Int) { G.glDeleteTextures(1,intArrayOf(id),0) }

    fun render(input:Int, external:Boolean, textureTransform:FloatArray, output:EGLSurface,
        width:Int, height:Int, mask:BeautyMask?, outputToAnalysis:BeautyTransform?,
        preset:BeautyPreset, freshness:Float) {
        makeCurrent(output)
        val amount=preset.blend*freshness.coerceIn(0f,1f)
        val copy=if(external) OES else COPY
        if(amount<=0f || mask==null || outputToAnalysis==null) {
            draw(copy,null,width,height,listOf(input),external) { uniformMatrix4("tx",textureTransform) }
            checkGl(); return
        }
        allocate(width,height)
        val full=targets[0]; val low=targets.drop(1)
        draw(copy,full,width,height,listOf(input),external) { uniformMatrix4("tx",textureTransform) }
        val w=low[0].w; val h=low[0].h
        fun blur(source:Int,target:Target,horizontal:Boolean,squared:Boolean=false) {
            draw(BLUR,target,w,h,listOf(source)) {
                uniform2("stepUv",if(horizontal) 1f/w else 0f,if(horizontal) 0f else 1f/h)
                uniform1("squared",if(squared) 1f else 0f)
            }
        }
        blur(full.texture,low[0],true); blur(low[0].texture,low[1],false)
        blur(full.texture,low[2],true,true); blur(low[2].texture,low[3],false)
        draw(COEFFICIENT,low[0],w,h,listOf(low[1].texture,low[3].texture)) { uniform1("coefficientB",0f) }
        draw(COEFFICIENT,low[2],w,h,listOf(low[1].texture,low[3].texture)) { uniform1("coefficientB",1f) }
        blur(low[0].texture,low[4],true); blur(low[4].texture,low[3],false)
        blur(low[2].texture,low[4],true); blur(low[4].texture,low[5],false)
        draw(COMPOSITE,null,width,height,listOf(full.texture,low[3].texture,low[5].texture)) {
            uniform1("amount",amount*(1f-preset.detail))
            uniform2("pixels",width.toFloat(),height.toFloat())
            val local=mask.analysisToLocal*outputToAnalysis
            G.glUniformMatrix3fv(G.glGetUniformLocation(this,"toLocal"),1,false,local.glValues(),0)
            fun ellipses(name:String,list:List<BeautyEllipse>) {
                require(list.size==4)
                G.glUniform4fv(G.glGetUniformLocation(this,name),4,
                    list.flatMap { listOf(it.x,it.y,it.rx,it.ry) }.toFloatArray(),0)
            }
            ellipses("skin",mask.skin); ellipses("protectedArea",mask.protected)
        }
        checkGl()
    }

    private fun allocate(w:Int,h:Int) {
        require(w > 0 && h > 0 && w.toLong()*h <= 8_294_400L) { "美颜预览尺寸超出资源上限" }
        if(size == (w to h)) return
        targets.forEach(::delete); targets.clear(); size=0 to 0
        fun add(tw:Int,th:Int,half:Boolean) {
            val tex=texture(); val fbo=IntArray(1)
            try {
                G.glTexImage2D(G.GL_TEXTURE_2D,0,if(half) G.GL_RGBA16F else G.GL_RGBA8,
                    tw,th,0,G.GL_RGBA,if(half) G.GL_HALF_FLOAT else G.GL_UNSIGNED_BYTE,null)
                G.glGenFramebuffers(1,fbo,0); G.glBindFramebuffer(G.GL_FRAMEBUFFER,fbo[0])
                G.glFramebufferTexture2D(G.GL_FRAMEBUFFER,G.GL_COLOR_ATTACHMENT0,G.GL_TEXTURE_2D,tex,0)
                check(G.glCheckFramebufferStatus(G.GL_FRAMEBUFFER)==G.GL_FRAMEBUFFER_COMPLETE) { "美颜 GPU 格式不支持" }
                targets+=Target(tex,fbo[0],tw,th)
            } catch(e:Throwable) { deleteTexture(tex); G.glDeleteFramebuffers(1,fbo,0); throw e }
        }
        add(w,h,false); repeat(6) { add((w+3)/4,(h+3)/4,true) }; size=w to h
    }

    private fun draw(fragment:String,target:Target?,w:Int,h:Int,textures:List<Int>,
        external:Boolean=false, uniforms:Int.()->Unit={}) {
        val program=programs.getOrPut(fragment) { program(fragment) }
        G.glBindFramebuffer(G.GL_FRAMEBUFFER,target?.framebuffer ?: 0); G.glViewport(0,0,w,h)
        G.glUseProgram(program)
        textures.forEachIndexed { i,id ->
            G.glActiveTexture(G.GL_TEXTURE0+i)
            G.glBindTexture(if(external && i==0) EXTERNAL else G.GL_TEXTURE_2D,id)
            G.glUniform1i(G.glGetUniformLocation(program,"tex$i"),i)
        }
        program.uniforms(); G.glDrawArrays(G.GL_TRIANGLES,0,3)
    }
    private fun Int.uniform1(name:String,v:Float) { G.glUniform1f(G.glGetUniformLocation(this,name),v) }
    private fun Int.uniform2(name:String,x:Float,y:Float) { G.glUniform2f(G.glGetUniformLocation(this,name),x,y) }
    private fun Int.uniformMatrix4(name:String,v:FloatArray) { G.glUniformMatrix4fv(G.glGetUniformLocation(this,name),1,false,v,0) }
    private fun program(fragment:String):Int {
        fun shader(type:Int,source:String):Int {
            val id=G.glCreateShader(type); G.glShaderSource(id,source); G.glCompileShader(id)
            val status=IntArray(1); G.glGetShaderiv(id,G.GL_COMPILE_STATUS,status,0)
            if(status[0]==0) { val message=G.glGetShaderInfoLog(id); G.glDeleteShader(id); error(message) }
            return id
        }
        val vertex=shader(G.GL_VERTEX_SHADER,VERTEX)
        var frag=0; var program=0
        try {
            frag=shader(G.GL_FRAGMENT_SHADER,fragment); program=G.glCreateProgram()
            G.glAttachShader(program,vertex); G.glAttachShader(program,frag); G.glLinkProgram(program)
            val status=IntArray(1); G.glGetProgramiv(program,G.GL_LINK_STATUS,status,0)
            check(status[0]!=0) { G.glGetProgramInfoLog(program) }; return program
        } catch(e:Throwable) { if(program!=0) G.glDeleteProgram(program); throw e }
        finally { G.glDeleteShader(vertex); if(frag!=0) G.glDeleteShader(frag) }
    }
    private fun delete(target:Target) { deleteTexture(target.texture); G.glDeleteFramebuffers(1,intArrayOf(target.framebuffer),0) }
    override fun close() {
        if (disposed) return
        disposed = true
        try {
            makeCurrent(); targets.forEach(::delete); targets.clear()
            programs.values.forEach(G::glDeleteProgram); programs.clear()
        } finally { releaseEgl() }
    }
    private fun releaseEgl() {
        if (!initialized) return
        E.eglMakeCurrent(display,E.EGL_NO_SURFACE,E.EGL_NO_SURFACE,E.EGL_NO_CONTEXT)
        if (scratch != E.EGL_NO_SURFACE) E.eglDestroySurface(display,scratch)
        if (context != E.EGL_NO_CONTEXT) E.eglDestroyContext(display,context)
        E.eglReleaseThread()
        scratch = E.EGL_NO_SURFACE; context = E.EGL_NO_CONTEXT; initialized = false
    }
    private data class Target(val texture:Int,val framebuffer:Int,val w:Int,val h:Int)
    companion object {
        const val EXTERNAL=0x8D65
        fun identity() = FloatArray(16).also { android.opengl.Matrix.setIdentityM(it,0) }
        fun checkGl() { check(G.glGetError()==G.GL_NO_ERROR) { "美颜 GPU 处理失败" } }
        private val VERTEX="""#version 300 es
            out vec2 uv;
            void main(){ vec2 p=vec2(float((gl_VertexID<<1)&2),float(gl_VertexID&2)); uv=p; gl_Position=vec4(p*2.0-1.0,0,1); }
        """.trimIndent()
        private val HEADER="""#version 300 es
            precision highp float;
            in vec2 uv; out vec4 color;
        """.trimIndent()+"\n"
        private val COPY=HEADER+"uniform sampler2D tex0; uniform mat4 tx; void main(){color=texture(tex0,(tx*vec4(uv,0,1)).xy);}"
        private val OES="""#version 300 es
            #extension GL_OES_EGL_image_external_essl3 : require
            precision highp float;
            in vec2 uv; out vec4 color;
            uniform samplerExternalOES tex0; uniform mat4 tx;
            void main(){color=texture(tex0,(tx*vec4(uv,0,1)).xy);}
        """.trimIndent()
        private val BLUR=HEADER+"""
            uniform sampler2D tex0; uniform vec2 stepUv; uniform float squared;
            void main(){vec3 sum=vec3(0); for(int i=-2;i<=2;i++){vec3 p=texture(tex0,uv+float(i)*stepUv).rgb; sum+=mix(p,p*p,squared);} color=vec4(sum/5.0,1);}
        """.trimIndent()
        private val COEFFICIENT=HEADER+"""
            uniform sampler2D tex0; uniform sampler2D tex1; uniform float coefficientB;
            void main(){vec3 mean=texture(tex0,uv).rgb; vec3 variance=max(texture(tex1,uv).rgb-mean*mean,vec3(0)); vec3 a=variance/(variance+vec3(0.0025)); color=vec4(mix(a,mean*(1.0-a),coefficientB),1);}
        """.trimIndent()
        private val COMPOSITE=HEADER+"""
            uniform sampler2D tex0; uniform sampler2D tex1; uniform sampler2D tex2;
            uniform vec4 skin[4]; uniform vec4 protectedArea[4]; uniform mat3 toLocal;
            uniform vec2 pixels; uniform float amount;
            float ellipse(vec2 p,vec4 e){float t=clamp((1.0-length((p-e.xy)/e.zw))/0.30,0.0,1.0);return t*t*(3.0-2.0*t);}
            void main(){vec3 original=texture(tex0,uv).rgb; vec2 p=(toLocal*vec3(uv.x*pixels.x,(1.0-uv.y)*pixels.y,1)).xy;
                float m=0.0; float protect=0.0; for(int i=0;i<4;i++){m=max(m,ellipse(p,skin[i]));protect=max(protect,ellipse(p,protectedArea[i]));}
                vec3 smoothColor=texture(tex1,uv).rgb*original+texture(tex2,uv).rgb;
                color=vec4(mix(original,smoothColor,amount*m*(1.0-protect)),1);}
        """.trimIndent()
    }
}
