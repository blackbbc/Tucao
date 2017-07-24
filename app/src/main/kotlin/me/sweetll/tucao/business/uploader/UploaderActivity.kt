package me.sweetll.tucao.business.uploader

import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.Toolbar
import android.view.View
import me.sweetll.tucao.R
import me.sweetll.tucao.base.BaseActivity
import me.sweetll.tucao.business.uploader.adapter.VideoAdapter
import me.sweetll.tucao.databinding.ActivityUploaderBinding

class UploaderActivity : BaseActivity() {

    lateinit var binding: ActivityUploaderBinding

    lateinit var videoAdapter: VideoAdapter

    override fun getToolbar(): Toolbar = binding.toolbar

    companion object {
        fun intentTo(context: Context, userid: String) {
            val intent = Intent(context, UploaderActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_uploader)

        val data = mutableListOf<String>()
        (1..100).mapTo(data) { "$it" }
        videoAdapter = VideoAdapter(data)
        binding.videoRecycler.layoutManager = LinearLayoutManager(this)
        binding.videoRecycler.adapter = videoAdapter
    }

    override fun initToolbar() {
        super.initToolbar()
        supportActionBar?.let {
            it.setDisplayHomeAsUpEnabled(true)
            it.title = ""
        }
    }
}
