package me.sweetll.tucao.business.home.adapter

import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import me.sweetll.tucao.business.home.fragment.*

class HomePagerAdapter(fm : FragmentManager) : FragmentPagerAdapter(fm) {
    val tabTitles = listOf("推荐", "新番", "影剧", "游戏", "动画", "频道")

    override fun getItem(position: Int) =
        when (position) {
            0 -> RecommendFragment()
            1 -> BangumiFragment()
            2 -> MovieFragment()
            3 -> GameFragment()
            4 -> AnimationFragment()
            else -> ChannelListFragment()
        }

    override fun getCount() = tabTitles.size

    override fun getPageTitle(position: Int) = tabTitles[position]
}
