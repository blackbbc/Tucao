package me.sweetll.tucao

import android.app.Application

class AppApplication : Application() {
    val INSTANCE : AppApplication by lazy {
        this
    }

    fun get() : AppApplication {
        return INSTANCE
    }

    override fun onCreate() {
        super.onCreate()
    }

}
