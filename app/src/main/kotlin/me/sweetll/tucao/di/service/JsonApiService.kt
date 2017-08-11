package me.sweetll.tucao.di.service

import io.reactivex.Observable
import me.sweetll.tucao.business.video.model.ReplyResponse
import me.sweetll.tucao.model.json.BaseResponse
import me.sweetll.tucao.model.json.ListResponse
import me.sweetll.tucao.model.json.Result
import me.sweetll.tucao.model.json.Version
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

    @GET(ApiConfig.RANK_API_URL)
    fun rank(@Query("tid") tid: Int,
             @Query("date") date: Int): Observable<BaseResponse<Map<Int, Result>>>

    @GET(ApiConfig.UPDATE_API_URL)
    fun update(@Query("appKey") appKey: String,
               @Query("appSecret") appSecret: String,
               @Query("versionCode") versionCode: Int): Observable<Version>

    @GET(ApiConfig.REPLY_API_URL)
    fun reply(@Query("commentid") commentId: String,
              @Query("replyid") replyId: String,
              @Query("page") page: Int,
              @Query("num") num: Int): Observable<ReplyResponse>
}
