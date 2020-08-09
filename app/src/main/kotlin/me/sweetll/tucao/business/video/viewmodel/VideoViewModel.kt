package me.sweetll.tucao.business.video.viewmodel

import android.annotation.SuppressLint
import androidx.databinding.ObservableField
import com.trello.rxlifecycle2.kotlin.bindToLifecycle
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import me.sweetll.tucao.AppApplication
import me.sweetll.tucao.base.BaseViewModel
import me.sweetll.tucao.model.json.Part
import me.sweetll.tucao.model.json.Video
import me.sweetll.tucao.business.video.VideoActivity
import me.sweetll.tucao.di.service.ApiConfig
import me.sweetll.tucao.extension.*
import me.sweetll.tucao.model.xml.Durl
import me.sweetll.tucao.rxdownload.entity.DownloadStatus
import java.io.File
import java.io.FileOutputStream

class VideoViewModel(val activity: VideoActivity): BaseViewModel() {
    val video = ObservableField<Video>()

    var playUrlDisposable: Disposable? = null
    var danmuDisposable: Disposable? = null

    var currentPlayerId: String? = null

    constructor(activity: VideoActivity, video: Video) : this(activity) {
        this.video.set(video)
    }

    @SuppressLint("CheckResult")
    fun queryVideo(hid: String) {
        jsonApiService.view(hid)
                .bindToLifecycle(activity)
                .sanitizeJson()
                .subscribe({
                    video ->
                    this.video.set(video)
                    activity.loadVideo(video)
                }, {
                    error ->
                    error.printStackTrace()
                    activity.binding.player.loadText?.let {
                        it.text = it.text.replace("获取视频信息...".toRegex(), "获取视频信息...[失败]")
                    }
                })
    }

    fun queryPlayUrls(hid: String, part: Part) {
        if (playUrlDisposable != null && !playUrlDisposable!!.isDisposed) {
            playUrlDisposable!!.dispose()
        }
        if (danmuDisposable != null && !danmuDisposable!!.isDisposed) {
            danmuDisposable!!.dispose()
        }

        if (part.flag == DownloadStatus.COMPLETED) {
            activity.loadDurls(part.durls)
        } else if (part.file.isNotEmpty()) {
            if ("clicli" !in part.file) {
                // 这个视频是直传的
                activity.loadDurls(mutableListOf(Durl(url = part.file)))
            } else {
                // 这个视频来自clicli
                playUrlDisposable = jsonApiService.clicli(part.file)
                        .bindToLifecycle(activity)
                        .subscribeOn(Schedulers.io())
                        .flatMap {
                            clicli ->
                            if (clicli.code == 0) {
                                Observable.just(clicli.url)
                            } else {
                                Observable.error(Throwable("请求视频接口出错"))
                            }
                        }
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({
                            url ->
                            activity.loadDurls(mutableListOf(Durl(url=url)))
                        }, {
                            error ->
                            error.printStackTrace()
                            activity.binding.player.loadText?.let {
                                it.text = it.text.replace("解析视频地址...".toRegex(), "解析视频地址...[失败]")
                            }
                        })

            }
        } else {
            playUrlDisposable = xmlApiService.playUrl(part.type, part.vid, System.currentTimeMillis() / 1000)
                    .bindToLifecycle(activity)
                    .subscribeOn(Schedulers.io())
                    .flatMap {
                        response ->
                        if (response.durls.isNotEmpty()) {
                            Observable.just(response.durls)
                        } else {
                            Observable.error(Throwable("请求视频接口出错"))
                        }
                    }
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        duals ->
                        activity.loadDurls(duals)
                    }, {
                        error ->
                        error.printStackTrace()
                        activity.binding.player.loadText?.let {
                            it.text = it.text.replace("解析视频地址...".toRegex(), "解析视频地址...[失败]")
                        }
                    })
        }

        currentPlayerId = ApiConfig.generatePlayerId(hid, part.order)
        danmuDisposable = rawApiService.danmu(currentPlayerId!!, System.currentTimeMillis() / 1000)
            .bindToLifecycle(activity)
            .subscribeOn(Schedulers.io())
            .map {
                responseBody ->
                val outputFile = File.createTempFile("tucao", ".xml", AppApplication.get().cacheDir)
                val outputStream = FileOutputStream(outputFile)

                outputStream.write(responseBody.bytes())
                outputStream.flush()
                outputStream.close()
                outputFile.absolutePath
            }
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

    fun sendDanmu(stime: Float, message: String) {
        currentPlayerId?.let {
            rawApiService.sendDanmu(it, it, stime, message)
                    .bindToLifecycle(activity)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        // 发送成功
                    }, Throwable::printStackTrace)
        }
    }
}
