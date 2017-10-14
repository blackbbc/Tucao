package me.sweetll.tucao.business.drrr.adapter

import android.support.v4.content.ContextCompat
import android.text.Html
import android.text.Spannable
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ImageSpan
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import me.sweetll.tucao.R
import me.sweetll.tucao.business.drrr.DrrrDetailActivity
import me.sweetll.tucao.business.drrr.model.MultipleItem
import me.sweetll.tucao.business.drrr.model.Post
import me.sweetll.tucao.business.drrr.model.Reply
import me.sweetll.tucao.extension.logD
import me.sweetll.tucao.util.RelativeDateFormat
import me.sweetll.tucao.widget.GlideImageGetter

class ReplyAdapter(val activity: DrrrDetailActivity, data: MutableList<MultipleItem>): BaseMultiItemQuickAdapter<MultipleItem, BaseViewHolder>(data) {

    init {
        addItemType(MultipleItem.TYPE_POST, R.layout.item_post)
        addItemType(MultipleItem.TYPE_REPLY_NUM, R.layout.item_reply_num_divider)
        addItemType(MultipleItem.TYPE_REPLY, R.layout.item_reply)
    }

    override fun convert(helper: BaseViewHolder, item: MultipleItem) {
        "${helper.position}".logD()
        when (helper.itemViewType) {
            MultipleItem.TYPE_POST -> convertPost(helper, item.post())
            MultipleItem.TYPE_REPLY_NUM -> convertReplyDivider(helper, item.replyNum())
            MultipleItem.TYPE_REPLY -> convertReply(helper, item.reply())
        }
    }

    private fun convertPost(helper: BaseViewHolder, item: Post) {
        helper.setText(R.id.text_time, RelativeDateFormat.format(item.createDt))

        val contentView = helper.getView<TextView>(R.id.text_content)
        val html = Html.fromHtml(item.content, GlideImageGetter(mContext, contentView), null) as Spannable
        html.getSpans(0, html.length, ImageSpan::class.java).forEach {
            val start = html.getSpanStart(it)
            val end = html.getSpanEnd(it)
            val flags = html.getSpanFlags(it)
            html.setSpan(object : ClickableSpan() {
                override fun onClick(p0: View?) {
                    val thumb = it.source
                    val source = it.source.replace("thumb", "uploads")

                    activity.showSourceImage(thumb, source)
                }
            }, start, end, flags)
        }
        contentView.text = html
        contentView.movementMethod = LinkMovementMethod.getInstance()

        helper.setText(R.id.text_reply_num, "${item.replyNum}")
        helper.setText(R.id.text_vote_num, "${item.voteNum}")
        if (item.vote) {
            helper.getView<ImageView>(R.id.img_thumb_up).setColorFilter(ContextCompat.getColor(mContext, R.color.pink_500))
        } else {
            helper.getView<ImageView>(R.id.img_thumb_up).setColorFilter(ContextCompat.getColor(mContext, R.color.grey_600))
        }
    }

    private fun convertReplyDivider(helper: BaseViewHolder, replyNum: Int) {
        helper.setText(R.id.text_reply_num, "共${replyNum}条回复")
    }

    private fun convertReply(helper: BaseViewHolder, reply: Reply) {
        helper.setText(R.id.text_time, RelativeDateFormat.format(reply.createDt))

        val contentView = helper.getView<TextView>(R.id.text_content)
        val html = Html.fromHtml(reply.content, GlideImageGetter(mContext, contentView), null) as Spannable
        html.getSpans(0, html.length, ImageSpan::class.java).forEach {
            val start = html.getSpanStart(it)
            val end = html.getSpanEnd(it)
            val flags = html.getSpanFlags(it)
            html.setSpan(object : ClickableSpan() {
                override fun onClick(p0: View?) {
                    val thumb = it.source
                    val source = it.source.replace("thumb", "uploads")

                    activity.showSourceImage(thumb, source)
                }
            }, start, end, flags)
        }
        contentView.text = html
        contentView.movementMethod = LinkMovementMethod.getInstance()
    }

}
