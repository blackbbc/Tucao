package me.sweetll.tucao.model

open class BaseResponse<T> {
    var code: Int = 0
    var msg: String? = null
    var result: T? = null
}