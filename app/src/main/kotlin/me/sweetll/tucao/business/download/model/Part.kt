package me.sweetll.tucao.business.download.model

import com.chad.library.adapter.base.entity.MultiItemEntity
import me.sweetll.tucao.business.download.adapter.DownloadedVideoAdapter
import me.sweetll.tucao.model.xml.Durl

class Part(val title: String,
           val size: Long,
           val order: Int,
           val status: Int,
           val durls: MutableList<Durl>): MultiItemEntity {
    override fun getItemType(): Int = DownloadedVideoAdapter.TYPE_PART
}