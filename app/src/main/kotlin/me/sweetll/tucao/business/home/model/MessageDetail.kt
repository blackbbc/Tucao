package me.sweetll.tucao.business.home.model

import com.chad.library.adapter.base.entity.MultiItemEntity

class MessageDetail(var avatar: String,
                    var message: String,
                    var time: String,
                    var type: Int): MultiItemEntity {
    companion object {
        const val TYPE_LEFT = 1
        const val TYPE_RIGHT = 2
    }

    override fun getItemType() = type

}
