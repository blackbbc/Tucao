package me.sweetll.tucao.business.video.viewmodel

import com.trello.rxlifecycle2.kotlin.bindToLifecycle
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import me.sweetll.tucao.AppApplication
import me.sweetll.tucao.base.BaseViewModel
import me.sweetll.tucao.business.video.VideoActivity
import me.sweetll.tucao.di.service.ApiConfig
import me.sweetll.tucao.extension.logD
import me.sweetll.tucao.extension.sanitizeJson
import me.sweetll.tucao.extension.toast
import me.sweetll.tucao.model.json.Result
import java.io.File
import java.io.FileOutputStream

class VideoViewModel(val activity: VideoActivity): BaseViewModel() {
    lateinit var result: Result

    constructor(activity: VideoActivity, hid: String) : this(activity) {
        queryResult(hid)
    }

    constructor(activity: VideoActivity, result: Result) : this(activity) {
        this.result = result
        activity.loadResult(result)
    }

    fun queryResult(hid: String) {
        jsonApiService.view(hid)
                .bindToLifecycle(activity)
                .sanitizeJson()
                .subscribe({
                    result ->
                    this.result = result
                    activity.loadResult(result)
                }, {
                    error ->
                    error.printStackTrace()
                    error.message?.toast()
                })
    }

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
                    activity.binding.player.loadText?.let {
                        it.text = it.text.replace("解析视频地址...".toRegex(), "解析视频地址...[完成]")
                    }
                })

        rawApiService.danmu(ApiConfig.generatePlayerId(hid, part), System.currentTimeMillis() / 1000)
                .bindToLifecycle(activity)
                .subscribeOn(Schedulers.io())
                .map({
                    responseBody ->
                    val outputFile = File.createTempFile("tucao", ".xml", AppApplication.get().cacheDir)
                    val outputStream = FileOutputStream(outputFile)

                    outputStream.write(responseBody.bytes())
                    outputStream.flush()
                    outputStream.close()
                    outputFile.absolutePath
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    uri ->
                    activity.loadDanmuUri(uri)
                }, {
                    error ->
                    error.printStackTrace()
                    activity.binding.player.loadText?.let {
                        it.text = it.text.replace("全舰弹幕装填...".toRegex(), "全舰弹幕装填...[失败]")
                    }
                })
    }

}
