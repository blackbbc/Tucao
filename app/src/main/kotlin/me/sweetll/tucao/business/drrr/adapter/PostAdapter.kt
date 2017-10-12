package me.sweetll.tucao.business.drrr.adapter

import android.support.v4.content.ContextCompat
import android.widget.ImageView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import me.sweetll.tucao.R
import me.sweetll.tucao.business.drrr.model.Post
import me.sweetll.tucao.util.RelativeDateFormat
import org.sufficientlysecure.htmltextview.HtmlHttpImageGetter
import org.sufficientlysecure.htmltextview.HtmlTextView

class PostAdapter(data: MutableList<Post>?): BaseQuickAdapter<Post, BaseViewHolder>(R.layout.item_post, data) {

    override fun convert(helper: BaseViewHolder, item: Post) {
        helper.setText(R.id.text_time, RelativeDateFormat.format(item.createDt))
        val htmlTextView = helper.getView<HtmlTextView>(R.id.text_content)
        htmlTextView.setHtml(item.content, HtmlHttpImageGetter(htmlTextView))
        helper.setText(R.id.text_reply_num, "${item.replyNum}")
        helper.setText(R.id.text_vote_num, "${item.voteNum}")
        if (item.vote) {
            helper.getView<ImageView>(R.id.img_thumb_up).setColorFilter(ContextCompat.getColor(mContext, R.color.pink_500))
        } else {
            helper.getView<ImageView>(R.id.img_thumb_up).setColorFilter(ContextCompat.getColor(mContext, R.color.grey_600))
        }
    }

}
