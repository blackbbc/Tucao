package me.sweetll.tucao.business.video.adapter

import androidx.core.content.ContextCompat
import android.view.View
import android.widget.CheckedTextView
import android.widget.ImageView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import me.sweetll.tucao.R
import me.sweetll.tucao.model.json.Part
import me.sweetll.tucao.rxdownload.entity.DownloadStatus

class PartAdapter(data: MutableList<Part>?) : BaseQuickAdapter<Part, BaseViewHolder>(R.layout.item_part, data) {
    override fun convert(helper: BaseViewHolder, part: Part) {
        val titleText = helper.getView<CheckedTextView>(R.id.checked_text_title)
        val downloadedImg = helper.getView<ImageView>(R.id.img_downloaded)
        val downloadingImg = helper.getView<ImageView>(R.id.img_downloading)

        titleText.text = part.title
        titleText.isChecked = part.checked
        if (part.checkDownload()) {
            // 存在
            if (part.flag == DownloadStatus.COMPLETED) {
                downloadedImg.visibility = View.VISIBLE
                downloadingImg.visibility = View.GONE
            } else {
                downloadedImg.visibility = View.GONE
                downloadingImg.visibility = View.VISIBLE
            }
        } else {
            // 不存在
            downloadedImg.visibility = View.GONE
            downloadingImg.visibility = View.GONE
        }

        if (part.hadPlay) {
            titleText.setTextColor(ContextCompat.getColor(mContext, R.color.selector_part2))
        } else {
            titleText.setTextColor(ContextCompat.getColor(mContext, R.color.selector_part))
        }
    }

}
