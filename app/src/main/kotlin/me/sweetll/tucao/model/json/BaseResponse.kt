package me.sweetll.tucao.model.json

open class BaseResponse<T> {
    var code: String? = null
    var msg: String? = null
    var result: T? = null
}