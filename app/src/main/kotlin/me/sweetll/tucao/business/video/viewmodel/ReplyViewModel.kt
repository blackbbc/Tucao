package me.sweetll.tucao.business.video.viewmodel

import com.trello.rxlifecycle2.kotlin.bindToLifecycle
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import me.sweetll.tucao.Const
import me.sweetll.tucao.base.BaseViewModel
import me.sweetll.tucao.business.video.ReplyActivity
import me.sweetll.tucao.di.service.ApiConfig
import me.sweetll.tucao.extension.sanitizeHtml
import me.sweetll.tucao.extension.toast

class ReplyViewModel(val activity: ReplyActivity, val commentId: String, val replyId: String): BaseViewModel() {

    val pageSize = 10
    var pageIndex = 1

    init {
        loadData()
    }

    fun loadData() {
        pageIndex = 0
        jsonApiService.reply(commentId, replyId, pageIndex, pageSize)
                .bindToLifecycle(activity)
                .retryWhen(ApiConfig.RetryWithDelay())
                .subscribeOn(Schedulers.io())
                .map {
                    response ->
                    response.data.map { it.value }.toMutableList()
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    data ->
                    pageIndex++
                    activity.loadData(data)
                }, {
                    error ->
                    error.printStackTrace()
                    error.message?.toast()
                })
    }

    fun loadMoreData() {
        jsonApiService.reply(commentId, replyId, pageIndex, pageSize)
                .bindToLifecycle(activity)
                .retryWhen(ApiConfig.RetryWithDelay())
                .subscribeOn(Schedulers.io())
                .map {
                    response ->
                    response.data.map { it.value }.toMutableList()
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    data ->
                    if (data.size < pageSize) {
                        activity.loadMoreData(data, Const.LOAD_MORE_END)
                    } else {
                        pageIndex++
                        activity.loadMoreData(data, Const.LOAD_MORE_COMPLETE)
                    }
                }, {
                    error ->
                    error.printStackTrace()
                    error.message?.toast()
                    activity.loadMoreData(null, Const.LOAD_MORE_FAIL)
                })
    }

    fun support(id: String) {
                    rawApiService.support(commentId, id)
                            .sanitizeHtml {
                                Object()
                            }
                            .subscribe({

                            }, {
                                error ->
                                error.printStackTrace()
                            })
    }
}

