package me.sweetll.tucao.business.drrr.model

data class DrrrResponse<out T>(val code: Int, val msg: String?, val data: T?, val total: Int?)
