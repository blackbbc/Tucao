package me.sweetll.tucao.business.download

import android.content.Context
import android.content.Intent
import androidx.databinding.DataBindingUtil
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import android.view.View
import me.sweetll.tucao.R
import me.sweetll.tucao.base.BaseActivity
import me.sweetll.tucao.databinding.ActivityDownloadSettingBinding

class DownloadSettingActivity: BaseActivity() {
    lateinit var binding: ActivityDownloadSettingBinding

    override fun getToolbar(): Toolbar = binding.toolbar

    override fun getStatusBar(): View = binding.statusBar

    override fun initView(savedInstanceState: Bundle?) {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_download_setting)

        fragmentManager.beginTransaction()
                .replace(R.id.contentFrame, DownloadSettingFragment())
                .commit()
    }

    companion object {
        fun intentTo(context: Context) {
            val intent = Intent(context, DownloadSettingActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun initToolbar() {
        super.initToolbar()
        delegate.supportActionBar?.let {
            it.title = "离线设置"
            it.setDisplayHomeAsUpEnabled(true)
        }
    }
}
