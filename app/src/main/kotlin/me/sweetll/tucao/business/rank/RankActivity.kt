package me.sweetll.tucao.business.rank

import android.content.Context
import android.content.Intent
import android.os.Bundle
import me.sweetll.tucao.R
import me.sweetll.tucao.base.BaseActivity

class RankActivity : BaseActivity() {
    companion object {
        fun intentTo(context: Context) {
            val intent = Intent(context, RankActivity::class.java)
            context.startActivity(intent)
        }
    }
    override fun initView(savedInstanceState: Bundle?) {
        setContentView(R.layout.activity_rank)
    }

}
