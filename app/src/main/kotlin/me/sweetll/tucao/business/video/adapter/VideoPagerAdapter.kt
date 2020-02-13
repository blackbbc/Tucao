package me.sweetll.tucao.business.video.adapter

import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import me.sweetll.tucao.model.json.Video
import me.sweetll.tucao.business.video.fragment.VideoCommentsFragment
import me.sweetll.tucao.business.video.fragment.VideoInfoFragment

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

    fun bindVideo(video: Video) {
        videoInfoFragment.bindVideo(video)
        videoCommentsFragment.bindVideo(video)
    }

}
