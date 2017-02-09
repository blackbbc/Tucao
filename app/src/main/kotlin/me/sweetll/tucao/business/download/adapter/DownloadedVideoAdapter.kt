package me.sweetll.tucao.business.download.adapter

import android.widget.CheckBox
import android.widget.ImageView
import android.widget.LinearLayout
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.chad.library.adapter.base.entity.MultiItemEntity
import me.sweetll.tucao.R
import me.sweetll.tucao.business.download.model.Part
import me.sweetll.tucao.business.download.model.Video
import me.sweetll.tucao.business.video.CachedVideoActivity
import me.sweetll.tucao.business.video.VideoActivity
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
                helper.setText(R.id.text_size, video.status.formatTotalSize)
                val thumbImg = helper.getView<ImageView>(R.id.img_thumb)
                thumbImg.load(video.thumb)

                helper.setVisible(R.id.checkbox, video.checkable)
                helper.getView<CheckBox>(R.id.checkbox).isChecked = video.checked
//                helper.getView<MaterialCheckBox>(R.id.checkbox).setOnCheckedChangedListener {
//                    view, isChecked ->
//                    video.checked = isChecked
//                }

                helper.itemView.setOnClickListener {
                    if (video.isExpanded) {
                        collapse(helper.adapterPosition)
                    } else {
                        expand(helper.adapterPosition)
                    }
                }
                helper.getView<LinearLayout>(R.id.linear_detail).setOnClickListener {
                    VideoActivity.intentTo(mContext, video.hid)
                }
            }
            TYPE_PART -> {
                val part = item as Part
                helper.setText(R.id.text_title, part.title)
                helper.setText(R.id.text_size, part.status.formatTotalSize)

                helper.setVisible(R.id.checkbox, part.checkable)
                helper.getView<CheckBox>(R.id.checkbox).isChecked = part.checked
//                helper.getView<MaterialCheckBox>(R.id.checkbox).setOnCheckedChangedListener {
//                    view, isChecked ->
//                    part.checked = isChecked
//                }

                helper.itemView.setOnClickListener {
                    view ->
                    CachedVideoActivity.intentTo(mContext, part)
                }
            }
        }
    }

}
