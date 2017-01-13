package me.sweetll.tucao.di.service

import io.reactivex.Observable
import me.sweetll.tucao.model.json.ListResponse
import me.sweetll.tucao.model.json.Result
import me.sweetll.tucao.model.xml.DanmuInfo
import me.sweetll.tucao.model.xml.Video
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface XmlApiService {
    @GET(ApiConfig.PLAY_URL_API_URL)
    fun playUrl(@Query("type") type: String,
             @Query("vid") vid: String,
             @Query("r") r: Long) : Observable<Video>

    @GET(ApiConfig.DANMU_API_URL)
    fun danmu(@Path("hid") hid: String,
              @Path("part") part: Int,
              @Query("r") r: Long) : Observable<DanmuInfo>
}
