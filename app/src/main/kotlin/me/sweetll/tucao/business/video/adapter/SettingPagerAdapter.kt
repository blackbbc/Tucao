package me.sweetll.tucao.business.video.adapter

import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import me.sweetll.tucao.business.video.fragment.SettingBlockViewFactory
import me.sweetll.tucao.business.video.fragment.SettingDanmuViewFactory
import me.sweetll.tucao.business.video.fragment.SettingPlayViewFactory
import me.sweetll.tucao.widget.DanmuVideoPlayer

class SettingPagerAdapter(val player: DanmuVideoPlayer): PagerAdapter() {

    val tabTitles = listOf("播放器设置", "弹幕设置", "弹幕屏蔽")

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object`
    }

    override fun getPageTitle(position: Int) = tabTitles[position]

    override fun getCount() = tabTitles.size

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val view = when(position) {
            0 -> SettingPlayViewFactory.create(player, container)
            1 -> SettingDanmuViewFactory.create(player, container)
            else -> SettingBlockViewFactory.create(player, container)
        }
        container.addView(view)
        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }
}
