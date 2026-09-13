package com.photocoach.app.camera

import android.opengl.EGL14 as E

/** The default display is process-shared, including CameraX owners outside our renderer classes.
 * Each renderer destroys its own contexts/surfaces; none may terminate this shared display.
 */
internal object CameraEglDisplay {
    private val display by lazy {
        E.eglGetDisplay(E.EGL_DEFAULT_DISPLAY).also {
            check(it!=E.EGL_NO_DISPLAY && E.eglInitialize(it,IntArray(2),0,IntArray(2),0))
        }
    }
    fun get()=display
}
