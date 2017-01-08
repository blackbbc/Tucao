package me.sweetll.tucao.di.service

import io.reactivex.Observable
import me.sweetll.tucao.model.ListResponse
import me.sweetll.tucao.model.Result
import retrofit2.http.GET
import retrofit2.http.Query

interface JsonApiService {
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
