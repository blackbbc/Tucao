package me.sweetll.tucao.business.download.adapter

import android.widget.ImageView
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.chad.library.adapter.base.entity.MultiItemEntity
import me.sweetll.tucao.R
import me.sweetll.tucao.business.download.model.Part
import me.sweetll.tucao.business.download.model.Video
import me.sweetll.tucao.extension.formatWithUnit
import me.sweetll.tucao.extension.load

class DownloadedVideoAdapter(data: MutableList<MultiItemEntity>?): BaseMultiItemQuickAdapter<MultiItemEntity, BaseViewHolder>(data) {
    companion object {
        const val TYPE_VIDEO = 0
        const val TYPE_PART = 1
    }

    init {
        addItemType(TYPE_VIDEO, R.layout.item_downloaded_video)
        addItemType(TYPE_PART, R.layout.item_downloaded_part)
    }

    override fun convert(helper: BaseViewHolder, item: MultiItemEntity) {
        when (helper.itemViewType) {
            TYPE_VIDEO -> {
                val video = item as Video
                helper.setText(R.id.text_title, video.title)
                helper.setText(R.id.text_size, video.size.formatWithUnit())
                val thumbImg = helper.getView<ImageView>(R.id.img_thumb)
                thumbImg.load(video.thumb)
                helper.itemView.setOnClickListener {
                    if (video.isExpanded) {
                        collapse(helper.adapterPosition)
                    } else {
                        expand(helper.adapterPosition)
                    }
                }
            }
            TYPE_PART -> {
                (item as Part).let {
                    helper.setText(R.id.text_title, it.title)
                    helper.setText(R.id.text_size, it.size.formatWithUnit())
                }
            }
        }
    }

}
