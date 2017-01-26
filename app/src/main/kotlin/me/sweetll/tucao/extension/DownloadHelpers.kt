package me.sweetll.tucao.extension

import com.chad.library.adapter.base.entity.MultiItemEntity
import me.sweetll.tucao.business.download.model.Part
import me.sweetll.tucao.business.download.model.Video

object DownloadHelpers {
    fun loadDownloadedVideos(): MutableList<MultiItemEntity> {
        val part1 = Part("P1", 1024, 0, 0, mutableListOf())
        val part2 = Part("P2", 1024, 0, 0, mutableListOf())
        val video1 = Video("11", "Video1", "", 2048)
        video1.addSubItem(part1)
        video1.addSubItem(part2)
        val video2 = Video("22", "Video2", "", 1024)
        val part3 = Part("P1", 1024, 0, 0, mutableListOf())
        video2.addSubItem(part3)

        val data = mutableListOf<MultiItemEntity>(video1, video2)

        return data
    }
}
