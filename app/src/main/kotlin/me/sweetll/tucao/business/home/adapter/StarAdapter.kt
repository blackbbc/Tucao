package me.sweetll.tucao.business.home.adapter

import android.widget.ImageView
import com.chad.library.adapter.base.BaseItemDraggableAdapter
import com.chad.library.adapter.base.BaseViewHolder
import me.sweetll.tucao.R
import me.sweetll.tucao.model.json.Video
import me.sweetll.tucao.extension.formatByWan
import me.sweetll.tucao.extension.load

class StarAdapter(data: MutableList<Video>?): BaseItemDraggableAdapter<Video, BaseViewHolder>(R.layout.item_star, data) {
    override fun convert(helper: BaseViewHolder, item: Video) {
        helper.setText(R.id.text_title, item.title)
        helper.setText(R.id.text_play, item.play.formatByWan())
        helper.setText(R.id.text_mukio, item.mukio.formatByWan())

        val thumbImg = helper.getView<ImageView>(R.id.img_thumb)
        thumbImg.load(mContext, item.thumb)
    }
}
