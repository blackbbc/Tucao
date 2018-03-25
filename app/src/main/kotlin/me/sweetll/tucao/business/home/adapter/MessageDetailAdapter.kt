package me.sweetll.tucao.business.home.adapter

import android.widget.ImageView
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import me.sweetll.tucao.R
import me.sweetll.tucao.business.home.model.MessageDetail
import me.sweetll.tucao.extension.load

/**
 * Created by sweet on 3/25/18.
 */
class MessageDetailAdapter(data: MutableList<MessageDetail>?): BaseMultiItemQuickAdapter<MessageDetail, BaseViewHolder>(data) {

    init {
        addItemType(MessageDetail.TYPE_LEFT, R.layout.item_message_left)
        addItemType(MessageDetail.TYPE_RIGHT, R.layout.item_message_right)
    }

    override fun convert(helper: BaseViewHolder, item: MessageDetail) {
        helper.getView<ImageView>(R.id.img_avatar).load(mContext, item.avatar, R.drawable.default_avatar)
        helper.setText(R.id.text_message, item.message)
    }

}