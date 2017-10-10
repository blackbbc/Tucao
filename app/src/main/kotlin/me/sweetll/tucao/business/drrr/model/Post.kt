package me.sweetll.tucao.business.drrr.model

data class Post(
        val id: String,
        val content: String,
        val voteNum: Int,
        val replyNum: Int,
        val brand: String,
        val model: String,
        val systemVersion: String,
        val appVersion: String,
        val sticky: Boolean,
        val createDt: Long,
        val updateDt: Long,
        val vote: Boolean
)