package me.sweetll.tucao.business.drrr.model

import com.chad.library.adapter.base.entity.MultiItemEntity

class MultipleItem: MultiItemEntity {

    private var item: Any

    private var itemType: Int

    companion object {
        const val TYPE_POST = 1
        const val TYPE_REPLY_NUM = 2
        const val TYPE_REPLY = 3
    }

    constructor(post: Post) {
        item = post
        itemType = TYPE_POST
    }

    constructor(replyNum: Int) {
        item = replyNum
        itemType = TYPE_REPLY_NUM
    }

    constructor(reply: Reply) {
        item = reply
        itemType = TYPE_REPLY
    }

    override fun getItemType(): Int = itemType

    fun post() = item as Post

    fun post(post: Post) {
        item = post
        itemType = TYPE_POST
    }

    fun reply() = item as Reply

    fun reply(reply: Reply) {
        item = reply
        itemType = TYPE_REPLY
    }

    fun replyNum() = item as Int

    fun replyNum(replyNum: Int) {
        item = replyNum
        itemType = TYPE_REPLY_NUM
    }

}
