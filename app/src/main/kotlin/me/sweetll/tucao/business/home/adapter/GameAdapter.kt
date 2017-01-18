package me.sweetll.tucao.business.home.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import me.sweetll.tucao.R
import me.sweetll.tucao.model.json.Channel
import me.sweetll.tucao.model.json.Result

class GameAdapter(data: MutableList<Pair<Channel, List<Result>>>?): BaseQuickAdapter<Pair<Channel, List<Result>>, BaseViewHolder>(R.layout.item_recommend_video, data) {
    override fun convert(helper: BaseViewHolder, item: Pair<Channel, List<Result>>) {

    }
}
