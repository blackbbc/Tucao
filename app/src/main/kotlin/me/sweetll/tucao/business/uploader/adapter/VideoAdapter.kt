package me.sweetll.tucao.business.uploader.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import me.sweetll.tucao.R

class VideoAdapter(data: MutableList<String>?): BaseQuickAdapter<String, BaseViewHolder>(R.layout.item_simple_text, data) {
    override fun convert(helper: BaseViewHolder, item: String) {
        helper.setText(R.id.text1, item)
    }
}
