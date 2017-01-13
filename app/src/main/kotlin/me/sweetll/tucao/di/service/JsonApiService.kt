package me.sweetll.tucao.di.service

import io.reactivex.Observable
import me.sweetll.tucao.model.json.BaseResponse
import me.sweetll.tucao.model.json.ListResponse
import me.sweetll.tucao.model.json.Result
import retrofit2.http.GET
import retrofit2.http.Query

interface JsonApiService {
    @GET(ApiConfig.VIEW_API_URL)
    fun view(@Query("hid") hid: String): Observable<BaseResponse<Result>>

    @GET(ApiConfig.LIST_API_URL)
    fun list(@Query("tid") tid: Int,
             @Query("page") pageIndex: Int,
             @Query("pagesize") pageSize: Int,
             @Query("order") order: String?): Observable<ListResponse<Result>>

    @GET(ApiConfig.SEARCH_API_URL)
    fun search(@Query("tid") tid: Int?,
               @Query("page") pageIndex: Int,
               @Query("pagesize") pageSize: Int,
               @Query("order") order: String?,
               @Query("q") keyword: String): Observable<ListResponse<Result>>
}
