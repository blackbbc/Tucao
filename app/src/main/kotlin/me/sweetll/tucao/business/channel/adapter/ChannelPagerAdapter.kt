package me.sweetll.tucao.business.channel.adapter

import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import me.sweetll.tucao.business.channel.fragment.ChannelDetailFragment
import me.sweetll.tucao.model.json.Channel

class ChannelPagerAdapter(fm : FragmentManager, val channels: List<Channel>) : FragmentPagerAdapter(fm) {

    override fun getItem(position: Int) = ChannelDetailFragment.newInstance(channels[position].id)

    override fun getCount(): Int = channels.size

    override fun getPageTitle(position: Int) = channels[position].name
}
