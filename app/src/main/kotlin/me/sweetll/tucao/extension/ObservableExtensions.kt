package me.sweetll.tucao.extension

import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import me.sweetll.tucao.model.BaseResponse
import me.sweetll.tucao.model.ListResponse

fun <T> Observable<BaseResponse<T>>.sanitizeBase(): Observable<T?> = this
        .subscribeOn(io.reactivex.schedulers.Schedulers.io())
        .flatMap { response ->
            if (response.code == "200") {
                Observable.just(response.result)
            } else {
                Observable.error(Throwable(response.msg))
            }
        }
        .observeOn(AndroidSchedulers.mainThread())

fun <T> Observable<ListResponse<T>>.sanitizeList(): Observable<MutableList<T>?> = this
        .subscribeOn(io.reactivex.schedulers.Schedulers.io())
        .flatMap { response ->
            if (response.code == "200") {
                Observable.just(response.result)
            } else {
                Observable.error(Throwable(response.msg))
            }
        }
        .observeOn(AndroidSchedulers.mainThread())
