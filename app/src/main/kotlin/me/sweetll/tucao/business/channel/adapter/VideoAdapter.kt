package me.sweetll.tucao.business.channel.adapter

import android.widget.ImageView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import me.sweetll.tucao.R
import me.sweetll.tucao.extension.load
import me.sweetll.tucao.model.Result

class VideoAdapter(data: MutableList<Result>?): BaseQuickAdapter<Result, BaseViewHolder>(R.layout.item_video, data) {

    override fun convert(helper: BaseViewHolder, video: Result) {
        helper.setText(R.id.text_title, video.title)
        helper.setText(R.id.text_user, "up：${video.user}")
        helper.setText(R.id.text_play, "播放：${video.play}")
        helper.setText(R.id.text_mukio, "弹幕：${video.mukio}")

        val thumbImg: ImageView = helper.getView<ImageView>(R.id.img_thumb)
        thumbImg.load(video.thumb)
    }

}
