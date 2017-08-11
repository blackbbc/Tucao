package me.sweetll.tucao.business.video.model

class ReplyResponse(
        val commentid: String,
        val title: String,
        val total: String,
        val url: String,
        val lastupdate: String,
        val data: Map<String, Reply>
)
