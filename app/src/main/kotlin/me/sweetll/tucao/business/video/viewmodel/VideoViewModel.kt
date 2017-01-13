package me.sweetll.tucao.business.video.viewmodel

import com.trello.rxlifecycle2.kotlin.bindToLifecycle
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import me.sweetll.tucao.base.BaseViewModel
import me.sweetll.tucao.business.video.VideoActivity
import me.sweetll.tucao.di.service.ApiConfig
import me.sweetll.tucao.extension.logD
import me.sweetll.tucao.extension.toast
import me.sweetll.tucao.model.json.Result

class VideoViewModel(val activity: VideoActivity, val result: Result): BaseViewModel() {
    fun queryPlayUrls(hid: String, part: Int, type: String, vid: String) {
        xmlApiService.playUrl(type, vid, System.currentTimeMillis() / 1000)
                .bindToLifecycle(activity)
                .subscribeOn(Schedulers.io())
                .flatMap {
                    response ->
                    if ("succ" == response.result) {
                        Observable.just(response.durls)
                    } else {
                        Observable.error(Throwable("请求视频接口出错"))
                    }
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    duals ->
                    activity.loadDuals(duals)
                }, {
                    error ->
                    error.printStackTrace()
                    "请求视频接口出错".toast()
                })

//        xmlApiService.danmu(ApiConfig.generatePlayerId(hid, part), System.currentTimeMillis() / 1000)
//                .bindToLifecycle(activity)
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe({
//                    danmus ->
//                    danmus.toString().logD()
//                }, {
//                    error ->
//                    error.printStackTrace()
//                    "请求弹幕接口出错".toast()
//                })
    }

}
