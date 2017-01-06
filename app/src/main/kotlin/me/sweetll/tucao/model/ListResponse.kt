package me.sweetll.tucao.model

class ListResponse<T> : BaseResponse<MutableList<T>>() {
    var totalCount: Int = 0
}
