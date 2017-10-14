package me.sweetll.tucao.business.drrr.viewmodel

import android.view.View
import com.trello.rxlifecycle2.kotlin.bindToLifecycle
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import me.sweetll.tucao.Const
import me.sweetll.tucao.base.BaseViewModel
import me.sweetll.tucao.business.drrr.DrrrDetailActivity
import me.sweetll.tucao.business.drrr.DrrrNewPostActivity
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
        activity.setRefreshing(true)
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
                .doAfterTerminate { activity.setRefreshing(false) }
                .subscribe({
                    response ->
                    val data = response.data!!
                    val total = response.total!!
                    page++
                    activity.loadData(data.map { MultipleItem(it) }.toMutableList(), total)
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
                .doAfterTerminate { activity.setRefreshing(false) }
                .subscribe({
                    response ->
                    val data = response.data!!.map { MultipleItem(it) }
                    val total = response.total!!
                    if (data.size < size) {
                        activity.loadMoreData(data.toMutableList(), total, Const.LOAD_MORE_END)
                    } else {
                        page++
                        activity.loadMoreData(data.toMutableList(), total, Const.LOAD_MORE_COMPLETE)
                    }
                }, {
                    error ->
                    error.printStackTrace()
                    activity.loadMoreData(null, 0, Const.LOAD_MORE_FAIL)
                })
    }


    fun onClickAdd(view: View) {
        DrrrNewPostActivity.intentTo(activity, activity.post, DrrrDetailActivity.REQUEST_NEW_REPLY)
    }

}
