package me.sweetll.tucao.business.home.model

data class MessageList(var id: String,
                       var username: String,
                       var avatar: String,
                       var time: String,
                       var message: String,
                       var read: Boolean)