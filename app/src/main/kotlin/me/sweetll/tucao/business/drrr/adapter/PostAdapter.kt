package me.sweetll.tucao.business.drrr.adapter

import android.support.v4.content.ContextCompat
import android.text.Html
import android.text.Spannable
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ImageSpan
import android.view.View
import android.view.View.MeasureSpec
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import me.sweetll.tucao.R
import me.sweetll.tucao.business.drrr.DrrrListActivity
import me.sweetll.tucao.business.drrr.model.Post
import me.sweetll.tucao.util.RelativeDateFormat
import me.sweetll.tucao.widget.GlideImageGetter
import me.sweetll.tucao.widget.UrlDrawable

class PostAdapter(val activity: DrrrListActivity, data: MutableList<Post>?): BaseQuickAdapter<Post, BaseViewHolder>(R.layout.item_post, data) {

    override fun convert(helper: BaseViewHolder, item: Post) {
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
        helper.addOnClickListener(R.id.linear_thumb_up)

        if (item.vote) {
            helper.getView<ImageView>(R.id.img_thumb_up).setColorFilter(ContextCompat.getColor(mContext, R.color.pink_500))
        } else {
            helper.getView<ImageView>(R.id.img_thumb_up).setColorFilter(ContextCompat.getColor(mContext, R.color.grey_600))
        }
    }

}
