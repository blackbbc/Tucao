package me.sweetll.tucao.model.json

import com.google.gson.annotations.SerializedName

class ListResponse<T> : BaseResponse<MutableList<T>>() {
    @SerializedName("total_count")
    var totalCount: Int = 0
}
