package me.sweetll.tucao.business.download.adapter

import android.widget.ImageView
import android.widget.LinearLayout
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.chad.library.adapter.base.entity.MultiItemEntity
import me.sweetll.tucao.R
import me.sweetll.tucao.business.download.event.RefreshDownloadedVideoEvent
import me.sweetll.tucao.business.download.event.RefreshDownloadingVideoEvent
import me.sweetll.tucao.business.download.model.Part
import me.sweetll.tucao.business.download.model.StateController
import me.sweetll.tucao.business.download.model.Video
import me.sweetll.tucao.business.video.VideoActivity
import me.sweetll.tucao.extension.DownloadHelpers
import me.sweetll.tucao.extension.formatWithUnit
import me.sweetll.tucao.extension.load
import me.sweetll.tucao.extension.toast
import org.greenrobot.eventbus.EventBus
import zlc.season.rxdownload2.RxDownload
import zlc.season.rxdownload2.entity.DownloadFlag

class DownloadingVideoAdapter(data: MutableList<MultiItemEntity>?): BaseMultiItemQuickAdapter<MultiItemEntity, BaseViewHolder>(data) {
    companion object {
        const val TYPE_VIDEO = 0
        const val TYPE_PART = 1
    }

    val rxDownload: RxDownload by lazy {
        RxDownload.getInstance().context(mContext)
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
                helper.setVisible(R.id.text_size, false)
                val thumbImg = helper.getView<ImageView>(R.id.img_thumb)
                thumbImg.load(video.thumb)
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
                (item as Part).let {
                    it.stateController = StateController(helper.getView(R.id.text_size), helper.getView(R.id.img_status), helper.getView(R.id.progress))
                    helper.setText(R.id.text_title, it.title)
                    rxDownload.receiveDownloadStatus(it.durls[0].url)
                            .subscribe({
                                downloadEvent ->
                                it.flag = downloadEvent.flag
                                it.status = downloadEvent.downloadStatus
                                if (it.flag == DownloadFlag.COMPLETED) {
                                    DownloadHelpers.saveDownloadPart(it)
                                } else {
                                    it.stateController?.setEvent(downloadEvent)
                                }
                            }, {
                                error ->
                                error.printStackTrace()
                                error.message?.toast()
                            })
                    helper.itemView.setOnClickListener {
                        view ->
                        val callback = object: DownloadHelpers.Callback {
                            override fun startDownload() {
                                DownloadHelpers.startDownload(it)
                            }

                            override fun pauseDownload() {
                                DownloadHelpers.pauseDownload(it)
                            }

                        }
                        it.stateController?.handleClick(callback)
                    }
                }
            }
        }
    }

}
