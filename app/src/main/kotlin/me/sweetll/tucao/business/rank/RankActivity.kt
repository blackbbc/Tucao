package me.sweetll.tucao.business.rank

import android.content.Context
import android.content.Intent
import androidx.databinding.DataBindingUtil
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import android.view.View
import me.sweetll.tucao.R
import me.sweetll.tucao.base.BaseActivity
import me.sweetll.tucao.business.rank.adapter.RankPagerAdapter
import me.sweetll.tucao.business.rank.viewmodel.RankViewModel
import me.sweetll.tucao.databinding.ActivityRankBinding
import me.sweetll.tucao.model.json.Channel

class RankActivity : BaseActivity() {

    lateinit var binding: ActivityRankBinding
    val viewModel = RankViewModel(this)

    lateinit var parentChannels: List<Channel>

    override fun getToolbar(): Toolbar = binding.toolbar

    override fun getStatusBar(): View = binding.statusBar

    companion object {
        fun intentTo(context: Context) {
            val intent = Intent(context, RankActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_rank)
        binding.viewModel = viewModel

        parentChannels = Channel.findAllParentChannels()
        binding.viewPager.adapter = RankPagerAdapter(supportFragmentManager, parentChannels)
        binding.viewPager.offscreenPageLimit = parentChannels.size
        binding.tab.setupWithViewPager(binding.viewPager)
    }

    override fun initToolbar() {
        super.initToolbar()
        supportActionBar?.let {
            it.setDisplayShowTitleEnabled(false)
            it.setDisplayHomeAsUpEnabled(true)
        }
    }
}
