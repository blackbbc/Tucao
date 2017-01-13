package me.sweetll.tucao.di.service

import io.reactivex.Observable
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Query

interface RawApiService {
    @GET(ApiConfig.DANMU_API_URL)
    fun danmu(@Query("playerID") playerId: String,
              @Query("r") r: Long) : Observable<ResponseBody>
}
