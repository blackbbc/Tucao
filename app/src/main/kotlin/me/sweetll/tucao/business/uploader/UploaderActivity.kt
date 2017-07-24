package me.sweetll.tucao.business.uploader

import android.content.Context
import android.content.Intent
import android.os.Bundle
import me.sweetll.tucao.R
import me.sweetll.tucao.base.BaseActivity

class UploaderActivity : BaseActivity() {
    companion object {
        fun intentTo(context: Context, userid: String) {
            val intent = Intent(context, UploaderActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun initView(savedInstanceState: Bundle?) {
        setContentView(R.layout.activity_uploader)
    }
}
