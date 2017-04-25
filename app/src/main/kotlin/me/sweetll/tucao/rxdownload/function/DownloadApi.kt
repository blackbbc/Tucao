package me.sweetll.tucao.rxdownload.function

import io.reactivex.Observable
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Streaming
import retrofit2.http.Url

interface DownloadApi {
    @GET
    @Streaming
    fun download(
            @Url url: String,
            @Header("Range") range: String?,
            @Header("If-Range") ifRange: String?): Observable<Response<ResponseBody>>
}
