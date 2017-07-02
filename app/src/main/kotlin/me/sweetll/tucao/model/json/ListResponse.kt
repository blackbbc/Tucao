package me.sweetll.tucao.model.json

import com.squareup.moshi.Json

class ListResponse<T> : BaseResponse<MutableList<T>>() {
    @Json(name = "total_count")
    var totalCount: Int = 0
}
