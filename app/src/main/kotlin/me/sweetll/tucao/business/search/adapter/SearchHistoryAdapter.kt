package me.sweetll.tucao.business.search.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import me.sweetll.tucao.R
import me.sweetll.tucao.model.json.Video

class SearchHistoryAdapter(data: MutableList<Video>?): BaseQuickAdapter<Video, BaseViewHolder>(R.layout.item_search_history, data) {

    override fun convert(helper: BaseViewHolder, item: Video) {
        helper.setText(R.id.text_title, item.title)
        helper.addOnClickListener(R.id.img_delete)
    }

}
