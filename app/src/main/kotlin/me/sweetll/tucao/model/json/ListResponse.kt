package me.sweetll.tucao.model.json

import com.fasterxml.jackson.annotation.JsonProperty

class ListResponse<T> : BaseResponse<MutableList<T>>() {
    @JsonProperty("total_count")
    var totalCount: Int = 0
}
