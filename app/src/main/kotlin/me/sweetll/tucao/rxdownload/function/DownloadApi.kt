package me.sweetll.tucao.rxdownload.function

import io.reactivex.Observable
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface DownloadApi {
    @GET
    @Streaming
    @Headers("User-Agent: bilibili")
    fun download(
            @Url url: String,
            @Header("Range") range: String?,
            @Header("If-Range") ifRange: String?): Observable<Response<ResponseBody>>

    @GET
    fun downloadDanmu(
            @Url url: String): Observable<ResponseBody>
}
