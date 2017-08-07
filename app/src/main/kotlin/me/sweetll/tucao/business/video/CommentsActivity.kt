package me.sweetll.tucao.business.video

import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.view.View

import me.sweetll.tucao.R
import me.sweetll.tucao.base.BaseActivity
import me.sweetll.tucao.databinding.ActivityCommentsBinding

class CommentsActivity : BaseActivity() {
    lateinit var binding: ActivityCommentsBinding

    override fun getStatusBar(): View = binding.statusBar

    override fun getToolbar(): Toolbar = binding.toolbar

    companion object {
        fun intentTo(context: Context, options: Bundle?) {
            val intent = Intent(context, CommentsActivity::class.java)
            context.startActivity(intent, options)
        }
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_comments)
    }

    override fun initToolbar() {
        super.initToolbar()
        supportActionBar?.let {
            it.title = "AC1234567"
        }
    }
}
