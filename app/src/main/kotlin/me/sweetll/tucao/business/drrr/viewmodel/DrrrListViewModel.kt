package me.sweetll.tucao.business.drrr.viewmodel

import android.view.View
import com.trello.rxlifecycle2.kotlin.bindToLifecycle
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import me.sweetll.tucao.Const
import me.sweetll.tucao.base.BaseViewModel
import me.sweetll.tucao.business.drrr.DrrrListActivity
import me.sweetll.tucao.business.drrr.DrrrNewPostActivity
import me.sweetll.tucao.business.drrr.model.Post
import me.sweetll.tucao.di.service.ApiConfig

class DrrrListViewModel(val activity: DrrrListActivity): BaseViewModel() {

    var page = 0
    val size = 10

    init {
        loadData()
    }

    fun loadData() {
        page = 0
        activity.setRefreshing(true)
        jsonApiService.drrrPosts(page, size, "date", "desc")
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
                .doAfterTerminate { activity.setRefreshing(false) }
                .subscribe({
                    data ->
                    page++
                    activity.loadData(data.toMutableList())
                }, {
                    error ->
                    error.printStackTrace()
                })
    }

    fun loadMoreData() {
        jsonApiService.drrrPosts(page, size, "date", "desc")
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
                    val data = response.data!!
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

    fun onClickAdd(view: View) {
        DrrrNewPostActivity.intentTo(activity, DrrrListActivity.REQUEST_NEW_POST)
    }

    fun vote(post: Post) {
        jsonApiService.drrrVote(post.id)
                .bindToLifecycle(activity)
                .retryWhen(ApiConfig.RetryWithDelay())
                .subscribeOn(Schedulers.io())
                .subscribe({}, {})
    }
}
