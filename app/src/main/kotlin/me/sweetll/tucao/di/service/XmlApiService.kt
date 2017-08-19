package me.sweetll.tucao.di.service

import io.reactivex.Observable
import me.sweetll.tucao.model.xml.Video
import retrofit2.http.GET
import retrofit2.http.Query

interface XmlApiService {
    @GET(ApiConfig.PLAY_URL_API_URL)
    fun playUrl(@Query("type") type: String,
             @Query("vid") vid: String,
             @Query("r") r: Long) : Observable<Video>
}
