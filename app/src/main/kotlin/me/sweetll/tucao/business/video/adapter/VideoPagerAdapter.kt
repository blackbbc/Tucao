package me.sweetll.tucao.business.video.adapter

import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import me.sweetll.tucao.business.video.fragment.VideoCommentsFragment
import me.sweetll.tucao.business.video.fragment.VideoInfoFragment
import me.sweetll.tucao.model.json.Result

class VideoPagerAdapter(fm: FragmentManager): FragmentPagerAdapter(fm) {
    val tabTitles = listOf("简介", "评论")

    val videoInfoFragment = VideoInfoFragment()
    val videoCommentsFragment = VideoCommentsFragment()

    override fun getItem(position: Int) =
        when (position) {
            0 -> videoInfoFragment
            else -> videoCommentsFragment
        }

    override fun getCount() = tabTitles.size

    override fun getPageTitle(position: Int) = tabTitles[position]

    fun bindResult(result: Result) {
        videoInfoFragment.bindResult(result)
        videoCommentsFragment.bindResult(result)
    }

}
