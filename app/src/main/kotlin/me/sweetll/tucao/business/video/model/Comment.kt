package me.sweetll.tucao.business.video.model

data class Comment(val avatar: String,
                   val level: String,
                   val nickname: String,
                   var thumbUp: Int,
                   val lch: String,
                   val time: String,
                   val info: String,
                   val id: String,
                   val replyNum: Int,
                   var hasSend: Boolean = true,
                   var support: Boolean = false)
