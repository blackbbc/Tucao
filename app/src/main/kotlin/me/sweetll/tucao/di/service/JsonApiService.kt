package me.sweetll.tucao.di.service

import io.reactivex.Observable
import me.sweetll.tucao.model.ListResponse
import me.sweetll.tucao.model.ListResult
import retrofit2.http.GET
import retrofit2.http.Query

interface JsonApiService {
    @GET(ApiUrl.LIST_API_URL)
    fun list(@Query("tid") tid: Int,
             @Query("page") page: Int,
             @Query("pagesize") pageSize: Int,
             @Query("order") order: String?) : Observable<ListResponse<ListResult>>
}
