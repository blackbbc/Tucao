package me.sweetll.tucao.business.uploader.adapter

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import me.sweetll.tucao.R
import me.sweetll.tucao.model.json.Video
import me.sweetll.tucao.extension.formatByWan
import me.sweetll.tucao.extension.load

class VideoAdapter(data: MutableList<Video>?): BaseQuickAdapter<Video, BaseViewHolder>(R.layout.item_video, data) {
    var lastAnimatedPosition = -1
    var animationsLocked = false
    var delayEnterAnimation = true

    override fun convert(helper: BaseViewHolder, video: Video) {
        helper.setText(R.id.text_title, video.title)
//        helper.setText(R.id.text_user, "up：${video.user}")
        helper.setText(R.id.text_play, "播放：${video.play.formatByWan()}")
        helper.setText(R.id.text_mukio, "弹幕：${video.mukio.formatByWan()}")

        helper.setTag(R.id.text_title, video.thumb)
        val thumbImg: ImageView = helper.getView<ImageView>(R.id.img_thumb)
        thumbImg.load(mContext, video.thumb)
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        runEnterAnimation(holder.itemView, position)
    }

    private fun runEnterAnimation(view: View, position: Int) {
        if (animationsLocked) return
        if (position > lastAnimatedPosition) {
            lastAnimatedPosition = position
            view.translationY = 100f
            view.alpha = 0f
            view.animate()
                    .translationY(0f)
                    .alpha(1f)
                    .setStartDelay(if (delayEnterAnimation) 20L * position else 20L)
                    .setInterpolator(DecelerateInterpolator())
                    .setDuration(300)
                    .setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator?) {
                            animationsLocked = true
                        }
                    })
                    .start()
        }
    }
}
