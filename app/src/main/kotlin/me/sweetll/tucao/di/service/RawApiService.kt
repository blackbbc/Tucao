package me.sweetll.tucao.di.service

import io.reactivex.Observable
import okhttp3.ResponseBody
import retrofit2.http.*

interface RawApiService {
    @GET(ApiConfig.DANMU_API_URL)
    fun danmu(@Query("playerID") playerId: String,
              @Query("r") r: Long) : Observable<ResponseBody>

    @GET(ApiConfig.INDEX_URL)
    @Headers("Cookie: tucao_verify=ok")
    fun index() : Observable<ResponseBody>

    @GET(ApiConfig.LIST_URL)
    @Headers("Cookie: tucao_verify=ok")
    fun list(@Path("tid") tid: Int): Observable<ResponseBody>

    @GET(ApiConfig.BGM_URL)
    @Headers("Cookie: tucao_verify=ok")
    fun bgm(@Path("year") year: Int,
            @Path("month") month: Int): Observable<ResponseBody>
}
