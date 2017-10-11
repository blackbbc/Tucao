package me.sweetll.tucao.business.drrr.viewmodel

import com.trello.rxlifecycle2.kotlin.bindToLifecycle
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import me.sweetll.tucao.Const
import me.sweetll.tucao.base.BaseViewModel
import me.sweetll.tucao.business.drrr.DrrrDetailActivity
import me.sweetll.tucao.business.drrr.model.MultipleItem
import me.sweetll.tucao.di.service.ApiConfig

class DrrrDetailViewModel(val activity: DrrrDetailActivity): BaseViewModel() {

    var page = 0
    var size = 10

    init {
        loadData()
    }

    fun loadData() {
        page = 0
        jsonApiService.drrrReplies(activity.post.id, page, size)
                .bindToLifecycle(activity)
                .retryWhen(ApiConfig.RetryWithDelay())
                .subscribeOn(Schedulers.io())
                .flatMap {
                    response ->
                    if (response.code == 0) {
                        Observable.just(response.data!!)
                    } else {
                        Observable.error(Error(response.msg))
                    }
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    data ->
                    page++
                    activity.loadData(data.map { MultipleItem(it) }.toMutableList())
                }, {
                    error ->
                    error.printStackTrace()
                })
    }

    fun loadMoreData() {
        jsonApiService.drrrReplies(activity.post.id, page, size)
                .bindToLifecycle(activity)
                .retryWhen(ApiConfig.RetryWithDelay())
                .subscribeOn(Schedulers.io())
                .flatMap {
                    response ->
                    if (response.code == 0) {
                        Observable.just(response)
                    } else {
                        Observable.error(Error(response.msg))
                    }
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    response ->
                    val data = response.data!!.map { MultipleItem(it) }
                    if (data.size < size) {
                        activity.loadMoreData(data.toMutableList(), Const.LOAD_MORE_END)
                    } else {
                        page++
                        activity.loadMoreData(data.toMutableList(), Const.LOAD_MORE_COMPLETE)
                    }
                }, {
                    error ->
                    error.printStackTrace()
                    activity.loadMoreData(null, Const.LOAD_MORE_FAIL)
                })
    }
}
