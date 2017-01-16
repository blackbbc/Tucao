package me.sweetll.tucao.business.home.adapter

import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import me.sweetll.tucao.business.home.fragment.*

class HomePagerAdapter(fm : FragmentManager) : FragmentPagerAdapter(fm) {
    val tabTiles = mutableListOf("推荐", "新番", "动画", "游戏", "影剧", "频道")

    override fun getItem(position: Int) =
        when (position) {
            0 -> RecommendFragment()
            1 -> BangumiFragment()
            2 -> AnimationFragment()
            3 -> GameFragment()
            4 -> MovieFragment()
            else -> ChannelListFragment()
        }

    override fun getCount(): Int = tabTiles.size

    override fun getPageTitle(position: Int) = tabTiles[position]
}
