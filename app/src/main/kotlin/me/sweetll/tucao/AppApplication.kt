package me.sweetll.tucao

import android.app.Application
import me.drakeet.library.CrashWoodpecker
import me.drakeet.library.PatchMode
import me.sweetll.tucao.di.component.ApiComponent
import me.sweetll.tucao.di.component.BaseComponent
import me.sweetll.tucao.di.component.DaggerBaseComponent
import me.sweetll.tucao.di.module.ApiModule
import me.sweetll.tucao.di.module.BaseModule

class AppApplication : Application() {
    companion object {
        private lateinit var INSTANCE: AppApplication

        fun get(): AppApplication {
            return INSTANCE
        }
    }

    private lateinit var baseComponent: BaseComponent
    private lateinit var apiComponent: ApiComponent

    override fun onCreate() {
        super.onCreate()
        INSTANCE = this
        CrashWoodpecker.instance()
                .setPatchMode(PatchMode.SHOW_LOG_PAGE)
                .flyTo(this)
        initApiComponent()
    }

    private fun initApiComponent() {
        baseComponent = DaggerBaseComponent.builder()
                .baseModule(BaseModule())
                .build()
        apiComponent = baseComponent.plus(
                ApiModule()
        )
    }

    fun getApiComponent(): ApiComponent = apiComponent

}
