package me.sweetll.tucao.business.video.adapter

import android.widget.CheckedTextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import me.sweetll.tucao.R
import me.sweetll.tucao.model.json.Video

class PartAdapter(data: MutableList<Video>?) : BaseQuickAdapter<Video, BaseViewHolder>(R.layout.item_part, data) {
    override fun convert(helper: BaseViewHolder, part: Video) {
        (helper.convertView as CheckedTextView).let {
            it.text = part.title
            it.isChecked = part.checked
        }
    }
}
