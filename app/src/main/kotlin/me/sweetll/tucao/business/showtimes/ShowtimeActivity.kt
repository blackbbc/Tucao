package me.sweetll.tucao.business.showtimes

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import me.sweetll.tucao.R
import me.sweetll.tucao.base.BaseActivity

class ShowtimeActivity : BaseActivity() {

    companion object {
        fun intentTo(context: Context) {
            context.startActivity(Intent(context, ShowtimeActivity::class.java))
        }
    }

    override fun initView(savedInstanceState: Bundle?) {

    }

}
