package me.sweetll.tucao.business.rank.adapter

import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import me.sweetll.tucao.business.rank.fragment.RankDetailFragment
import me.sweetll.tucao.model.json.Channel

class RankPagerAdapter(fm : FragmentManager, val channels: List<Channel>) : FragmentPagerAdapter(fm) {

    override fun getItem(position: Int) = RankDetailFragment.newInstance(channels[position].id)

    override fun getCount(): Int = channels.size

    override fun getPageTitle(position: Int) = channels[position].name
}
