package me.sweetll.tucao.model

open class BaseResponse<T> {
    var code: Int = 0
    var result: T? = null
}