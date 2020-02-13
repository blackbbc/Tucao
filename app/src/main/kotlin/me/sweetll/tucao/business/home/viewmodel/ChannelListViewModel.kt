package me.sweetll.tucao.business.home.viewmodel

import android.view.View
import androidx.fragment.app.Fragment
import me.sweetll.tucao.business.channel.ChannelDetailActivity

class ChannelListViewModel(val fragment: Fragment) {

    fun onClickChannel(view: View) {
        val tid = Integer.parseInt(view.tag as String)
        ChannelDetailActivity.intentTo(fragment.activity!!, tid)
    }
}