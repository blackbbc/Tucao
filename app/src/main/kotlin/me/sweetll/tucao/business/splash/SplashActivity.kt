package me.sweetll.tucao.business.splash

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler

import me.sweetll.tucao.business.home.MainActivity

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!isTaskRoot
                && intent.hasCategory(Intent.CATEGORY_LAUNCHER)
                && intent.action != null
                && intent.action == Intent.ACTION_MAIN) {
            finish()
            return
        }

        Handler().postDelayed({
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        }, 1500)

    }
}
