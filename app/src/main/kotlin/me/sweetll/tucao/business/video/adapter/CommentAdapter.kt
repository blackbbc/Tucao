package me.sweetll.tucao.business.video.adapter

import android.widget.ImageView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import me.sweetll.tucao.R
import me.sweetll.tucao.business.video.model.Comment
import me.sweetll.tucao.extension.load

class CommentAdapter(data: MutableList<Comment>?) : BaseQuickAdapter<Comment, BaseViewHolder>(R.layout.item_comment, data) {
    override fun convert(helper: BaseViewHolder, comment: Comment) {
        helper.getView<ImageView>(R.id.img_avatar).load(mContext, comment.avatar)
        helper.setText(R.id.text_level, comment.level)
        helper.setText(R.id.text_nickname, comment.nickname)
        helper.setText(R.id.text_lch, comment.lch)
        helper.setText(R.id.text_time, comment.time)
        helper.setText(R.id.text_thumb_up, "${comment.thumbUp}")
        helper.setText(R.id.text_info, comment.info)
    }
}
