package me.sweetll.tucao.rxdownload.function

import io.reactivex.Observable
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface DownloadApi {

    @GET
    @Streaming
    @Headers("User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.87 Safari/537.36")
    fun download(
            @Url url: String,
            @Header("Range") range: String?,
            @Header("If-Range") ifRange: String?): Observable<Response<ResponseBody>>

    @GET
    fun downloadDanmu(
            @Url url: String): Observable<ResponseBody>
}
