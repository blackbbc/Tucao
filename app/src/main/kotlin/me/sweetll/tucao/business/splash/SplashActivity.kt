package me.sweetll.tucao.business.splash

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import io.reactivex.schedulers.Schedulers
import me.sweetll.tucao.AppApplication

import me.sweetll.tucao.business.home.MainActivity
import me.sweetll.tucao.di.service.ApiConfig
import me.sweetll.tucao.di.service.RawApiService
import javax.inject.Inject

class SplashActivity : AppCompatActivity() {

    @Inject
    lateinit var rawApiService: RawApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AppApplication.get()
                .getApiComponent()
                .inject(this)

        if (!isTaskRoot
                && intent.hasCategory(Intent.CATEGORY_LAUNCHER)
                && intent.action != null
                && intent.action == Intent.ACTION_MAIN) {
            finish()
            return
        }

        // Just to fetch cookie
        rawApiService.userInfo()
                .retryWhen(ApiConfig.RetryWithDelay())
                .subscribeOn(Schedulers.io())
                .subscribe({}, {})

        Handler().postDelayed({
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        }, 1500)

    }
}
