package me.sweetll.tucao.business.search.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import me.sweetll.tucao.R
import me.sweetll.tucao.model.json.Result

class SearchHistoryAdapter(data: MutableList<Result>?): BaseQuickAdapter<Result, BaseViewHolder>(R.layout.item_search_history, data) {

    override fun convert(helper: BaseViewHolder, item: Result) {
        helper.setText(R.id.text_title, item.title)
        helper.addOnClickListener(R.id.img_delete)
    }

}
