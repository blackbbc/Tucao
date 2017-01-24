package me.sweetll.tucao.business.home.adapter

import android.widget.ImageView
import com.chad.library.adapter.base.BaseItemDraggableAdapter
import com.chad.library.adapter.base.BaseViewHolder
import me.sweetll.tucao.R
import me.sweetll.tucao.extension.load
import me.sweetll.tucao.model.json.Result

class PlayHistoryAdapter(data: MutableList<Result>?): BaseItemDraggableAdapter<Result, BaseViewHolder>(R.layout.item_play_history, data) {
    override fun convert(helper: BaseViewHolder, item: Result) {
        helper.setText(R.id.text_title, item.title)
        helper.setText(R.id.text_creat, item.creat)

        val thumbImg = helper.getView<ImageView>(R.id.img_thumb)
        thumbImg.load(item.thumb)
    }
}
