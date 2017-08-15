package me.sweetll.tucao.business.video.adapter

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import me.sweetll.tucao.business.video.fragment.SettingBlockFragment
import me.sweetll.tucao.business.video.fragment.SettingDanmuFragment
import me.sweetll.tucao.business.video.fragment.SettingPlayFragment

class SettingPagerAdapter(fm: FragmentManager): FragmentPagerAdapter(fm) {
    val tabTitles = listOf("播放器设置", "弹幕设置", "弹幕屏蔽")

    val settingPlayFragment = SettingPlayFragment()
    val settingDanmuFragment = SettingDanmuFragment()
    val settingBlockFragment = SettingBlockFragment()

    override fun getItem(position: Int) =
        when (position) {
            0 -> settingPlayFragment
            1 -> settingDanmuFragment
            else -> settingBlockFragment
        }

    override fun getPageTitle(position: Int) = tabTitles[position]

    override fun getCount() = tabTitles.size

}
