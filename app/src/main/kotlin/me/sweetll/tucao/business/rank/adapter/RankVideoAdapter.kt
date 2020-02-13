package me.sweetll.tucao.business.rank.adapter

import androidx.core.content.ContextCompat
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import me.sweetll.tucao.R
import me.sweetll.tucao.model.json.Video
import me.sweetll.tucao.extension.formatByWan
import me.sweetll.tucao.extension.load

class RankVideoAdapter(data: MutableList<Video>?): BaseQuickAdapter<Video, BaseViewHolder>(R.layout.item_video, data) {

    override fun convert(helper: BaseViewHolder, video: Video) {
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
                rankText.setTextColor(ContextCompat.getColor(mContext, R.color.pink_400))
            }
            3 -> {
                rankText.textSize = 20f
                rankText.setTextColor(ContextCompat.getColor(mContext, R.color.pink_300))
            }
            else -> {
                rankText.textSize = 18f
                rankText.setTextColor(ContextCompat.getColor(mContext, R.color.secondary_text))
            }
        }

        val thumbImg: ImageView = helper.getView<ImageView>(R.id.img_thumb)
        thumbImg.load(mContext, video.thumb)
    }

}
