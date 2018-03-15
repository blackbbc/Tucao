package me.sweetll.tucao.business.home.viewmodel

import android.support.v4.app.Fragment
import android.view.View
import me.sweetll.tucao.business.channel.ChannelDetailActivity

class ChannelListViewModel(val fragment: Fragment) {

    fun onClickChannel(view: View) {
        val tid = Integer.parseInt(view.tag as String)
        ChannelDetailActivity.intentTo(fragment.activity!!, tid)
    }
}