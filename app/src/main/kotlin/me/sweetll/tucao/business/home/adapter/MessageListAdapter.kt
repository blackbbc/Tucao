package me.sweetll.tucao.business.home.adapter

import android.widget.ImageView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import me.sweetll.tucao.R
import me.sweetll.tucao.business.home.model.MessageList
import me.sweetll.tucao.extension.load

class MessageListAdapter(data: MutableList<MessageList>?):
        BaseQuickAdapter<MessageList, BaseViewHolder>(R.layout.item_message_list, data) {
    override fun convert(helper: BaseViewHolder, item: MessageList) {
        helper.getView<ImageView>(R.id.img_avatar).load(mContext, item.avatar, R.drawable.default_avatar)
        helper.setText(R.id.text_username, item.username)
        helper.setText(R.id.text_time, item.time)
        helper.setText(R.id.text_message, item.message)
    }
}