package me.sweetll.tucao.business.channel

import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.view.View
import me.sweetll.tucao.R
import me.sweetll.tucao.base.BaseActivity
import me.sweetll.tucao.business.channel.adapter.ChannelPagerAdapter
import me.sweetll.tucao.business.channel.viewmodel.ChannelDetailViewModel
import me.sweetll.tucao.databinding.ActivityChannelDetailBinding
import me.sweetll.tucao.model.Channel

class ChannelDetailActivity : BaseActivity() {
    lateinit var binding: ActivityChannelDetailBinding
    val detailViewModel: ChannelDetailViewModel by lazy { ChannelDetailViewModel(this) }

    var tid = 0
    lateinit var channel: Channel
    lateinit var parentChannel: Channel
    lateinit var siblingChannels: List<Channel>

    companion object {
        private val ARG_TID = "arg_tid"

        fun intentTo(context: Context, tid: Int) {
            val intent = Intent(context, ChannelDetailActivity::class.java)
            intent.putExtra(ARG_TID, tid)
            context.startActivity(intent)
        }
    }

    override fun getToolbar(): Toolbar = binding.toolbar

    override fun getStatusBar(): View = binding.statusBar

    override fun initView(savedInstanceState: Bundle?) {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_channel_detail)
        binding.viewModel = detailViewModel

        tid = intent.getIntExtra(ARG_TID, 0)
        channel = Channel.find(tid)!!
        parentChannel = Channel.find(channel.getValidParentId())!!
        siblingChannels = Channel.findSiblingChannels(parentChannel.id)

        binding.viewPager.adapter = ChannelPagerAdapter(supportFragmentManager, siblingChannels)
        binding.viewPager.offscreenPageLimit = siblingChannels.size
        binding.tab.setupWithViewPager(binding.viewPager)
        val selectedTabPosition = siblingChannels.indexOf(channel)
        binding.tab.getTabAt(selectedTabPosition)?.select()
    }

    override fun initToolbar() {
        super.initToolbar()
        supportActionBar?.let {
            it.title = parentChannel.name
            it.setDisplayHomeAsUpEnabled(true)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
