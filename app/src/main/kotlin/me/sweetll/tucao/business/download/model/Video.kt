package me.sweetll.tucao.business.download.model

import com.chad.library.adapter.base.entity.AbstractExpandableItem
import com.chad.library.adapter.base.entity.MultiItemEntity
import me.sweetll.tucao.business.download.adapter.DownloadedVideoAdapter

class Video(val hid: String,
            val title: String,
            val thumb: String,
            val size: Long): AbstractExpandableItem<Part>(), MultiItemEntity {
    override fun getLevel(): Int = 0

    override fun getItemType(): Int = DownloadedVideoAdapter.TYPE_VIDEO
}
