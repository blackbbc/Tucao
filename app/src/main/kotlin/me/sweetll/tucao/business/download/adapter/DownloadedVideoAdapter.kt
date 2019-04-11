package me.sweetll.tucao.business.download.adapter

import android.widget.CheckBox
import android.widget.ImageView
import android.widget.LinearLayout
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.chad.library.adapter.base.entity.MultiItemEntity
import me.sweetll.tucao.R
import me.sweetll.tucao.business.download.DownloadActivity
import me.sweetll.tucao.model.json.Part
import me.sweetll.tucao.model.json.Video
import me.sweetll.tucao.business.video.CachedVideoActivity
import me.sweetll.tucao.business.video.VideoActivity
import me.sweetll.tucao.extension.formatWithUnit
import me.sweetll.tucao.extension.load

class DownloadedVideoAdapter(val downloadActivity: DownloadActivity, data: MutableList<MultiItemEntity>?) : BaseMultiItemQuickAdapter<MultiItemEntity, BaseViewHolder>(data) {
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
                helper.setText(R.id.text_size, video.totalSize.formatWithUnit())
                val thumbImg = helper.getView<ImageView>(R.id.img_thumb)
                thumbImg.load(mContext, video.thumb)

                helper.setGone(R.id.checkbox, video.checkable)
                val checkBox = helper.getView<CheckBox>(R.id.checkbox)
                checkBox.isChecked = video.checked
                checkBox.setOnCheckedChangeListener { _, checked ->
                    video.checked = checked
                    updateMenu()
                }

                helper.itemView.setOnClickListener {
                    if (video.checkable) {
                        checkBox.isChecked = !checkBox.isChecked
                        video.parts.forEach {
                            it.checked = checkBox.isChecked
                        }
                        if (video.isExpanded) {
                            notifyItemRangeChanged(helper.adapterPosition + 1, video.parts.size)
                        }
                        updateMenu()
                    } else {
                        if (video.singlePart) {
                            CachedVideoActivity.intentTo(mContext, video)
                        } else {
                            if (video.isExpanded) {
                                collapse(helper.adapterPosition)
                            } else {
                                expand(helper.adapterPosition)
                            }
                        }
                    }
                }
                helper.getView<LinearLayout>(R.id.linear_detail).setOnClickListener {
                    VideoActivity.intentTo(mContext, video.hid)
                }
            }
            TYPE_PART -> {
                val part = item as Part
                helper.setText(R.id.text_title, part.title)
                helper.setText(R.id.text_size, part.totalSize.formatWithUnit())

                helper.setGone(R.id.checkbox, part.checkable)
                val checkBox = helper.getView<CheckBox>(R.id.checkbox)
                checkBox.isChecked = part.checked
                checkBox.setOnCheckedChangeListener { _, checked ->
                    part.checked = checked
                    updateMenu()
                }

                helper.itemView.setOnClickListener {
                    val parentVideo = data.find { video ->
                        (video as Video).parts.any { it.vid == part.vid }
                    } as Video
                    if (part.checkable) {
                        checkBox.isChecked = !checkBox.isChecked

                        val currentPosition = parentVideo.parts.indexOf(part)
                        val newParentChecked = parentVideo.parts.all(Part::checked)
                        if (newParentChecked != parentVideo.checked) {
                            parentVideo.checked = newParentChecked
                            notifyItemChanged(helper.adapterPosition - 1 - currentPosition)
                        }
                        updateMenu()
                    } else {
                        CachedVideoActivity.intentTo(mContext, parentVideo.apply { parts = mutableListOf(parts.find { it.vid == part.vid }!! ) })
                    }
                }
            }
        }
    }

    fun updateMenu() {
        val deleteEnabled = data.any {
            when (it) {
                is Video -> it.checked
                is Part -> it.checked
                else -> false
            }
        }
        val isPickAll = data.all {
            when (it) {
                is Video -> it.checked
                is Part -> it.checked
                else -> false
            }
        }
        downloadActivity.updateBottomMenu(deleteEnabled, isPickAll)
    }


}
