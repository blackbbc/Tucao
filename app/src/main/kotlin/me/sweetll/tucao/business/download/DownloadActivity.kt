package me.sweetll.tucao.business.download

import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.view.View
import me.sweetll.tucao.R
import me.sweetll.tucao.base.BaseActivity
import me.sweetll.tucao.business.download.adapter.DownloadPagerAdapter
import me.sweetll.tucao.databinding.ActivityDownloadBinding

class DownloadActivity : BaseActivity() {
    lateinit var binding: ActivityDownloadBinding

    override fun getStatusBar(): View = binding.statusBar

    override fun getToolbar(): Toolbar = binding.toolbar

    companion object {
        fun intentTo(context: Context) {
            val intent = Intent(context, DownloadActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_download)

        binding.viewPager.adapter = DownloadPagerAdapter(supportFragmentManager)
        binding.viewPager.offscreenPageLimit = 2
        binding.tab.setupWithViewPager(binding.viewPager)
    }

    override fun initToolbar() {
        super.initToolbar()
        supportActionBar?.let {
            it.title = "离线缓存"
            it.setDisplayHomeAsUpEnabled(true)
        }
    }
}
