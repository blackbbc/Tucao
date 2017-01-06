package me.sweetll.tucao.business.channel

import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.widget.Toolbar
import me.sweetll.tucao.R
import me.sweetll.tucao.base.BaseActivity
import me.sweetll.tucao.business.channel.viewmodel.ChannelViewModel
import me.sweetll.tucao.databinding.ActivityChannelBinding

class ChannelActivity : BaseActivity() {
    lateinit var binding: ActivityChannelBinding
    val viewModel: ChannelViewModel by lazy { ChannelViewModel(this) }
    var tid = 0

    companion object {
        private val ARG_TID = "arg_tid"

        fun intentTo(context: Context, tid: Int) {
            val intent = Intent(context, ChannelActivity::class.java)
            intent.putExtra(ARG_TID, tid)
            context.startActivity(intent)
        }
    }

    override fun getToolbar(): Toolbar = binding.toolbar


    override fun initToolbar() {
        super.initToolbar()
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_channel)
        binding.viewModel = viewModel

        tid = intent.getIntExtra(ARG_TID, 0)
    }
}
