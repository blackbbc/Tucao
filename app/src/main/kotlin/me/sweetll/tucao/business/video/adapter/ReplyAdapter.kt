package me.sweetll.tucao.business.video.adapter

import androidx.core.content.ContextCompat
import android.widget.ImageView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import me.sweetll.tucao.R
import me.sweetll.tucao.business.video.model.Reply
import me.sweetll.tucao.extension.load
import me.sweetll.tucao.util.RelativeDateFormat

class ReplyAdapter(data: MutableList<Reply>?): BaseQuickAdapter<Reply, BaseViewHolder>(R.layout.item_comment, data) {
    override fun convert(helper: BaseViewHolder, reply: Reply) {
        helper.getView<ImageView>(R.id.img_avatar).load(mContext, reply.avatar, R.drawable.default_avatar)
        helper.setGone(R.id.text_level, false)
        helper.setText(R.id.text_nickname, reply.nickname)
        helper.setGone(R.id.text_lch, false)
        helper.setText(R.id.text_time, RelativeDateFormat.format(reply.time))
        helper.setText(R.id.text_info, reply.content)
        helper.setGone(R.id.linear_thumb_up, false)
        helper.setGone(R.id.linear_reply, false)
        helper.addOnClickListener(R.id.linear_thumb_up)
        if (reply.hasSend) {
            helper.setTextColor(R.id.text_info, ContextCompat.getColor(mContext, R.color.primary_text))
        } else {
            helper.setTextColor(R.id.text_info, ContextCompat.getColor(mContext, R.color.secondary_text))
        }
    }
}
