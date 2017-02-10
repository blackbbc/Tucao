package me.sweetll.tucao.model.json

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
open class BaseResponse<T> {
    var code: String? = null
    var msg: String? = null
    var result: T? = null
}