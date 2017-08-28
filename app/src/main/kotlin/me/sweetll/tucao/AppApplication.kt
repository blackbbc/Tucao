package me.sweetll.tucao

import android.support.multidex.MultiDexApplication
import android.support.v7.app.AppCompatDelegate
import com.github.moduth.blockcanary.BlockCanary
import com.raizlabs.android.dbflow.config.FlowManager
import com.shuyu.gsyvideoplayer.utils.PlayerConfig
//import com.squareup.leakcanary.LeakCanary
import com.umeng.analytics.MobclickAgent
import me.drakeet.library.CrashWoodpecker
import me.drakeet.library.PatchMode
import me.sweetll.tucao.di.component.ApiComponent
import me.sweetll.tucao.di.component.BaseComponent
import me.sweetll.tucao.di.component.DaggerBaseComponent
import me.sweetll.tucao.di.component.UserComponent
import me.sweetll.tucao.di.module.ApiModule
import me.sweetll.tucao.di.module.BaseModule
import me.sweetll.tucao.di.module.UserModule
import me.sweetll.tucao.di.service.ApiConfig
import me.sweetll.tucao.extension.UpdateHelpers

class AppApplication : MultiDexApplication() {
    companion object {
        private lateinit var INSTANCE: AppApplication

        fun get(): AppApplication {
            return INSTANCE
        }

        init {
            AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        }
    }

    private lateinit var baseComponent: BaseComponent
    private lateinit var apiComponent: ApiComponent
    private lateinit var userComponent: UserComponent

    override fun onCreate() {
        super.onCreate()
        INSTANCE = this

        // For performance analysis
        /*
        BlockCanary.install(this, AppBlockCanaryContext()).start()
        CrashWoodpecker.instance()
                .setPatchMode(PatchMode.SHOW_LOG_PAGE)
                .flyTo(this)
        if (LeakCanary.isInAnalyzerProcess(this)) {
            return
        }
        LeakCanary.install(this)
        */

        MobclickAgent.setScenarioType(this, MobclickAgent.EScenarioType.E_UM_NORMAL)
        FlowManager.init(this)
        PlayerConfig.init(this)
        initComponent()

        postUpdate()
    }

    private fun postUpdate() {
        if (UpdateHelpers.newVersion()) {
            /*
             * Do some processing to prevent crash due to incompatible old data model
             */
            try {
                if (UpdateHelpers.needClearUserData()) {
                    UpdateHelpers.clearUserData()
                }
            } catch (error: Error) {
                error.printStackTrace()
            } finally {
                UpdateHelpers.updateVersion()
            }
        }
    }

    private fun initComponent() {
        baseComponent = DaggerBaseComponent.builder()
                .baseModule(BaseModule(ApiConfig.API_KEY))
                .build()
        apiComponent = baseComponent.plus(
                ApiModule()
        )
        userComponent = apiComponent.plus(
                UserModule()
        )
    }

    fun getApiComponent(): ApiComponent = apiComponent

    fun getUserComponent(): UserComponent = userComponent
}
