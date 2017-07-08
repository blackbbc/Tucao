package me.sweetll.tucao.business.login

import android.content.Context
import android.content.Intent
import android.os.Bundle

import me.sweetll.tucao.R
import me.sweetll.tucao.base.BaseActivity

class LoginActivity : BaseActivity() {
    override fun initView(savedInstanceState: Bundle?) {
        setContentView(R.layout.activity_login)
    }

    companion object {
        fun intentTo(context: Context) {
            val intent = Intent(context, LoginActivity::class.java)
            context.startActivity(intent)
        }
    }

}
