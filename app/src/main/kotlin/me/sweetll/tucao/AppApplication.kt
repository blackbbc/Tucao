package me.sweetll.tucao

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.raizlabs.android.dbflow.config.FlowManager
import com.shuyu.gsyvideoplayer.utils.PlayerConfig
import com.umeng.analytics.MobclickAgent
import me.sweetll.tucao.extension.UpdateHelpers
import androidx.appcompat.app.AppCompatDelegate
import androidx.multidex.MultiDexApplication
import dagger.android.AndroidInjection
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import me.sweetll.tucao.di.component.*
import me.sweetll.tucao.di.service.ApiConfig
import javax.inject.Inject


class AppApplication : MultiDexApplication(), HasAndroidInjector {
    companion object {
        const val PRIMARY_CHANNEL = "${BuildConfig.APPLICATION_ID}_CHANNEL_PRIMARY"

        private lateinit var INSTANCE: AppApplication

        fun get(): AppApplication {
            return INSTANCE
        }

        init {
            AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        }
    }

    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Any>

    private lateinit var apiComponent: ApiComponent
    private lateinit var userComponent: UserComponent

    override fun onCreate() {
        super.onCreate()
        INSTANCE = this

        // For performance analysis
        /*
        CrashWoodpecker.instance()
                .setPatchMode(PatchMode.SHOW_LOG_PAGE)
                .flyTo(this)
        if (LeakCanary.isInAnalyzerProcess(this)) {
            return
        }
        LeakCanary.install(this)
        */

        // disableHiddenApiCheck()

        MobclickAgent.setScenarioType(this, MobclickAgent.EScenarioType.E_UM_NORMAL)
        FlowManager.init(this)
        PlayerConfig.init(this)
        initComponent()
        initChannel()

        postUpdate()
    }

    @SuppressLint("PrivateApi")
    private fun disableHiddenApiCheck() {
        try {
            val cls = Class.forName("android.app.ActivityThread")
            val declaredMethod = cls.getDeclaredMethod("currentActivityThread")
            declaredMethod.isAccessible = true
            val activityThread = declaredMethod.invoke(null)
            val mHiddenApiWarningShown = cls.getDeclaredField("mHiddenApiWarningShown")
            mHiddenApiWarningShown.isAccessible = true
            mHiddenApiWarningShown.setBoolean(activityThread, true)
        } catch (e: Exception) {
            e.printStackTrace()
        }

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
        val networkComponent = DaggerNetworkComponent.factory().create(ApiConfig.API_KEY)
        apiComponent = networkComponent.apiComponent().create()
        userComponent = apiComponent.userComponent().create()

        userComponent.inject(this)
    }

    fun getApiComponent(): ApiComponent = apiComponent

    fun getUserComponent(): UserComponent = userComponent

    private fun initChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(PRIMARY_CHANNEL, "默认通知", NotificationManager.IMPORTANCE_DEFAULT)
            channel.enableVibration(false)
            channel.vibrationPattern = longArrayOf(0L)

            val notifyMgr = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notifyMgr.createNotificationChannel(channel)
        }
    }

    override fun androidInjector(): AndroidInjector<Any> {
        return dispatchingAndroidInjector
    }

}
