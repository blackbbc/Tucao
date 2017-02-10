package me.sweetll.tucao.model.json

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
class ListResponse<T> : BaseResponse<MutableList<T>>() {
    @JsonProperty("total_count")
    var totalCount: Int = 0
}
