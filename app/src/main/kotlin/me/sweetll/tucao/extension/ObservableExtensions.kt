package me.sweetll.tucao.extension

import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import io.reactivex.android.schedulers.AndroidSchedulers
import me.sweetll.tucao.di.service.ApiConfig
import me.sweetll.tucao.model.json.BaseResponse
import me.sweetll.tucao.model.json.ListResponse
import okhttp3.ResponseBody
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

fun <T> Observable<BaseResponse<T>>.sanitizeJson(): Observable<T> = this
        .subscribeOn(Schedulers.io())
        .retryWhen(ApiConfig.RetryWithDelay())
        .flatMap { response ->
            if (response.code == "200") {
                Observable.just(response.result!!)
            } else {
                Observable.error(Throwable(response.msg))
            }
        }
        .observeOn(AndroidSchedulers.mainThread())

fun <T> Observable<ListResponse<T>>.sanitizeJsonList(): Observable<ListResponse<T>> = this
        .subscribeOn(Schedulers.io())
        .retryWhen(ApiConfig.RetryWithDelay())
        .flatMap { response ->
            if (response.code == "200") {
                response.result = response.result ?: mutableListOf()
                Observable.just(response)
            } else {
                Observable.error(Throwable(response.msg))
            }
        }
        .observeOn(AndroidSchedulers.mainThread())

fun <T> Observable<ResponseBody>.sanitizeHtml(transform: Document.() ->  T): Observable<T> = this
        .subscribeOn(Schedulers.io())
        .retryWhen(ApiConfig.RetryWithDelay())
        .map {
            response ->
            Jsoup.parse(response.string()).transform()
        }
        .observeOn(AndroidSchedulers.mainThread())