package me.sweetll.tucao.business.channel.adapter

import android.widget.ImageView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import me.sweetll.tucao.R
import me.sweetll.tucao.model.json.Video
import me.sweetll.tucao.extension.formatByWan
import me.sweetll.tucao.extension.load

class VideoAdapter(data: MutableList<Video>?): BaseQuickAdapter<Video, BaseViewHolder>(R.layout.item_video, data) {

    override fun convert(helper: BaseViewHolder, video: Video) {
        helper.setText(R.id.text_title, video.title)
        helper.setText(R.id.text_user, "up：${video.user}")
        helper.setText(R.id.text_play, "播放：${video.play.formatByWan()}")
        helper.setText(R.id.text_mukio, "弹幕：${video.mukio.formatByWan()}")

        helper.setTag(R.id.text_title, video.thumb)
        val thumbImg: ImageView = helper.getView<ImageView>(R.id.img_thumb)
        thumbImg.load(mContext, video.thumb)
    }

}
