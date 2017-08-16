package me.sweetll.tucao.business.video.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import me.sweetll.tucao.R

class BlockListAdapter(data: MutableList<String>?): BaseQuickAdapter<String, BaseViewHolder>(R.layout.item_block_list, data) {
    override fun convert(helper: BaseViewHolder, item: String) {
        helper.setText(R.id.text_keyword, item)
        helper.addOnClickListener(R.id.img_close)
    }
}
