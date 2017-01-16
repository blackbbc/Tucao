package me.sweetll.tucao.business.rank.adapter

import android.support.v4.content.ContextCompat
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import me.sweetll.tucao.R
import me.sweetll.tucao.extension.formatByWan
import me.sweetll.tucao.extension.load
import me.sweetll.tucao.model.json.Result

class RankVideoAdapter(data: MutableList<Result>?): BaseQuickAdapter<Result, BaseViewHolder>(R.layout.item_video, data) {

    override fun convert(helper: BaseViewHolder, video: Result) {
        helper.setText(R.id.text_title, video.title)
        helper.setText(R.id.text_user, "up：${video.user}")
        helper.setText(R.id.text_play, "播放：${video.play.formatByWan()}")
        helper.setText(R.id.text_mukio, "弹幕：${video.mukio.formatByWan()}")

        val rank = data.indexOf(video) + 1
        val rankText = helper.getView<TextView>(R.id.text_rank)
        rankText.visibility = View.VISIBLE
        rankText.text = rank.toString()
        when (rank) {
            1 -> {
                rankText.textSize = 24f
                rankText.setTextColor(ContextCompat.getColor(mContext, R.color.pink_500))
            }
            2 -> {
                rankText.textSize = 22f
                rankText.setTextColor(ContextCompat.getColor(mContext, R.color.pink_300))
            }
            3 -> {
                rankText.textSize = 20f
                rankText.setTextColor(ContextCompat.getColor(mContext, R.color.pink_100))
            }
            else -> {
                rankText.textSize = 18f
                rankText.setTextColor(ContextCompat.getColor(mContext, R.color.secondary_text))
            }
        }

        val thumbImg: ImageView = helper.getView<ImageView>(R.id.img_thumb)
        thumbImg.load(video.thumb)
    }

}
