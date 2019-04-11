package me.sweetll.tucao.business.home.adapter

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import me.sweetll.tucao.R
import me.sweetll.tucao.model.json.Video
import me.sweetll.tucao.extension.formatByWan
import me.sweetll.tucao.extension.load
import me.sweetll.tucao.model.json.Channel

class MovieAdapter(data: MutableList<Pair<Channel, List<Video>>>?): BaseQuickAdapter<Pair<Channel, List<Video>>, BaseViewHolder>(R.layout.item_recommend_video, data) {
    override fun convert(helper: BaseViewHolder, item: Pair<Channel, List<Video>>) {
        val channel = item.first
        helper.setText(R.id.text_channel, channel.name)

        item.second.take(4).forEachIndexed {
            index, result ->
            val thumbImg: ImageView
            val playText: TextView
            val titleText: TextView
            when (index) {
                0 -> {
                    thumbImg = helper.getView(R.id.img_thumb1)
                    playText = helper.getView(R.id.text_play1)
                    titleText = helper.getView(R.id.text_title1)
                    helper.setTag(R.id.card1, result.hid)
                    helper.addOnClickListener(R.id.card1)
                }
                1 -> {
                    thumbImg = helper.getView(R.id.img_thumb2)
                    playText = helper.getView(R.id.text_play2)
                    titleText = helper.getView(R.id.text_title2)
                    helper.setTag(R.id.card2, result.hid)
                    helper.addOnClickListener(R.id.card2)
                }
                2 -> {
                    thumbImg = helper.getView(R.id.img_thumb3)
                    playText = helper.getView(R.id.text_play3)
                    titleText = helper.getView(R.id.text_title3)
                    helper.setTag(R.id.card3, result.hid)
                    helper.addOnClickListener(R.id.card3)
                }
                else -> {
                    thumbImg = helper.getView(R.id.img_thumb4)
                    playText = helper.getView(R.id.text_play4)
                    titleText = helper.getView(R.id.text_title4)
                    helper.setTag(R.id.card4, result.hid)
                    helper.addOnClickListener(R.id.card4)
                }
            }
            titleText.tag = result.thumb
            thumbImg.load(mContext, result.thumb)
            playText.text = result.play.formatByWan()
            titleText.text = result.title
        }

        for (index in item.second.size .. 3) {
            when (index) {
                0 -> {
                    helper.getView<View>(R.id.card1).visibility = View.INVISIBLE
                }
                1 -> {
                    helper.getView<View>(R.id.card2).visibility = View.INVISIBLE
                }
                2 -> {
                    helper.getView<View>(R.id.card3).visibility = View.INVISIBLE
                }
                else -> {
                    helper.getView<View>(R.id.card4).visibility = View.INVISIBLE
                }
            }
        }

        if (channel.id != 0) {
            helper.setText(R.id.text_more, "更多${channel.name}内容")
            helper.setTag(R.id.card_more, channel.id)
            helper.setGone(R.id.card_more, true)
            helper.setGone(R.id.img_rank, false)
            helper.addOnClickListener(R.id.card_more)
        }

    }
}
