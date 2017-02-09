package me.sweetll.tucao.business.video.adapter

import android.widget.CheckedTextView
import android.widget.ImageView
import android.view.View
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import me.sweetll.tucao.R
import me.sweetll.tucao.extension.DownloadHelpers
import me.sweetll.tucao.model.json.Video
import zlc.season.rxdownload2.entity.DownloadFlag

class DownloadPartAdapter(data: MutableList<Video>?) : BaseQuickAdapter<Video, BaseViewHolder>(R.layout.item_download_part, data) {
    override fun convert(helper: BaseViewHolder, part: Video) {
        val titleText = helper.getView<CheckedTextView>(R.id.checked_text_title)
        val downloadedImg = helper.getView<ImageView>(R.id.img_downloaded)
        val downloadingImg = helper.getView<ImageView>(R.id.img_downloading)

        val downloadParts = DownloadHelpers.loadDownloadVideos()
                .flatMap { (it as me.sweetll.tucao.business.download.model.Video).subItems }

        val existPart = downloadParts.find { it.vid == part.vid }

        titleText.text = part.title
        titleText.isChecked = part.checked
        if (existPart != null) {
            // 存在
            titleText.isEnabled = false
            if (existPart.flag == DownloadFlag.COMPLETED) {
                downloadedImg.visibility = View.VISIBLE
                downloadingImg.visibility = View.GONE
            } else {
                downloadedImg.visibility = View.GONE
                downloadingImg.visibility = View.VISIBLE
            }
        } else {
            // 不存在
            titleText.isEnabled = true
            downloadedImg.visibility = View.GONE
            downloadingImg.visibility = View.GONE
        }
    }
}
