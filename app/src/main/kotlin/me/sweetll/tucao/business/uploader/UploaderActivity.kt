package me.sweetll.tucao.business.uploader

import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.graphics.Color
import android.os.Bundle
import android.support.design.widget.AppBarLayout
import android.support.design.widget.CollapsingToolbarLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.Toolbar
import android.view.View
import me.sweetll.tucao.R
import me.sweetll.tucao.base.BaseActivity
import me.sweetll.tucao.business.uploader.adapter.VideoAdapter
import me.sweetll.tucao.business.uploader.viewmodel.UploaderViewModel
import me.sweetll.tucao.databinding.ActivityUploaderBinding
import me.sweetll.tucao.extension.load

class UploaderActivity : BaseActivity() {

    lateinit var binding: ActivityUploaderBinding

    lateinit var viewModel: UploaderViewModel

    lateinit var videoAdapter: VideoAdapter

    override fun getToolbar(): Toolbar = binding.toolbar

    companion object {
        const val ARG_USER_ID = "user_id"
        const val ARG_USERNAME = "username"
        const val ARG_AVATAR = "avatar"
        const val ARG_SIGNATURE = "signature"
        const val ARG_HEADER_BG = "header_bg"

        fun intentTo(context: Context, userId: String, username: String, avatar: String, signature: String, headerBg: String) {
            val intent = Intent(context, UploaderActivity::class.java)
            intent.putExtra(ARG_USER_ID, userId)
            intent.putExtra(ARG_USERNAME, username)
            intent.putExtra(ARG_AVATAR, avatar)
            intent.putExtra(ARG_SIGNATURE, signature)
            intent.putExtra(ARG_HEADER_BG, headerBg)
            context.startActivity(intent)
        }
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_uploader)
        viewModel = UploaderViewModel(this, intent.getStringExtra(ARG_USER_ID))
        binding.viewModel = viewModel

        val avatar = intent.getStringExtra(ARG_AVATAR)
        val username = intent.getStringExtra(ARG_USERNAME)
        val signature = intent.getStringExtra(ARG_SIGNATURE)
        val headerBg = intent.getStringExtra(ARG_HEADER_BG)

        binding.avatarImg.load(this, avatar, R.drawable.default_avatar)
        binding.usernameText.text = username
        binding.signatureText.text = signature
        binding.headerImg.load(this, headerBg)

        setupRecyclerView()

        binding.collapsingToolbar.setCollapsedTitleTextColor(Color.WHITE)
        binding.appBar.addOnOffsetChangedListener(object: AppBarLayout.OnOffsetChangedListener {
            val EXPANDED = 1 shl 0
            val COLLAPSED = 1 shl 1
            val IDLE = 1 shl 2

            var currentState = IDLE

            override fun onOffsetChanged(appBarLayout: AppBarLayout, i: Int) {
                if (i == 0) {
                    if (currentState != EXPANDED) {
                        binding.collapsingToolbar.title = " "
                    }
                    currentState = EXPANDED
                } else if (Math.abs(i) >= appBarLayout.totalScrollRange) {
                    if (currentState != COLLAPSED) {
                        binding.collapsingToolbar.title = username
                    }
                    currentState = COLLAPSED
                } else {
                    if (currentState != IDLE) {
                        binding.collapsingToolbar.title = " "
                    }
                    currentState = IDLE
                }
            }
        })
    }

    fun setupRecyclerView() {
        videoAdapter = VideoAdapter(null)
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
