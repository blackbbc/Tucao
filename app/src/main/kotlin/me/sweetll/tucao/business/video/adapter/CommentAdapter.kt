package me.sweetll.tucao.business.video.adapter

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import androidx.core.content.ContextCompat
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import me.sweetll.tucao.R
import me.sweetll.tucao.business.video.model.Comment
import me.sweetll.tucao.extension.load
import me.sweetll.tucao.util.RelativeDateFormat

class CommentAdapter(data: MutableList<Comment>?) : BaseQuickAdapter<Comment, BaseViewHolder>(R.layout.item_comment, data) {
    var lastAnimatedPosition = -1
    var animationsLocked = false
    var delayEnterAnimation = true

    override fun convert(helper: BaseViewHolder, comment: Comment) {
        helper.getView<ImageView>(R.id.img_avatar).load(mContext, comment.avatar, R.drawable.default_avatar)
        helper.setText(R.id.text_level, comment.level)
        helper.setText(R.id.text_nickname, comment.nickname)
        helper.setText(R.id.text_lch, comment.lch)
        helper.setText(R.id.text_time, RelativeDateFormat.format(comment.time))
        helper.setText(R.id.text_thumb_up, "${comment.thumbUp}")
        helper.setText(R.id.text_info, comment.info)
        helper.setText(R.id.text_reply_num, "${comment.replyNum}")
        helper.addOnClickListener(R.id.linear_thumb_up)
        if (comment.support) {
            helper.getView<ImageView>(R.id.img_thumb_up).setColorFilter(ContextCompat.getColor(mContext, R.color.pink_500))
        } else {
            helper.getView<ImageView>(R.id.img_thumb_up).setColorFilter(ContextCompat.getColor(mContext, R.color.grey_600))
        }
        if (comment.hasSend) {
            helper.setTextColor(R.id.text_info, ContextCompat.getColor(mContext, R.color.primary_text))
        } else {
            helper.setTextColor(R.id.text_info, ContextCompat.getColor(mContext, R.color.secondary_text))
        }
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
                    .setInterpolator(DecelerateInterpolator(2f))
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
