package me.sweetll.tucao.rxdownload2.function

import io.reactivex.Flowable
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Streaming
import retrofit2.http.Url

interface DownloadApi {
    @GET
    @Streaming
    fun download(@Header("Range") range: String,
                 @Header("If-Range") ifRange: String,
                 @Url url: String): Flowable<ResponseBody>
}
