package me.sweetll.tucao.business.home.viewmodel

import android.support.v4.app.Fragment
import android.view.View
import me.sweetll.tucao.business.channel.ChannelActivity

class ChannelViewModel(val fragment: Fragment) {

    fun onClickChannel(view: View) {
        val tid = view.tag as Int
        ChannelActivity.intentTo(fragment.activity, tid)
    }
}