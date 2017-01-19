package me.sweetll.tucao.di.service

import io.reactivex.Observable
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface RawApiService {
    @GET(ApiConfig.DANMU_API_URL)
    fun danmu(@Query("playerID") playerId: String,
              @Query("r") r: Long) : Observable<ResponseBody>

    @GET(ApiConfig.INDEX_URL)
    fun index() : Observable<ResponseBody>

    @GET(ApiConfig.LIST_URL)
    fun list(@Path("tid") tid: Int): Observable<ResponseBody>

    @GET(ApiConfig.BGM_URL)
    fun bgm(@Path("year") year: Int,
            @Path("month") month: Int): Observable<ResponseBody>
}
