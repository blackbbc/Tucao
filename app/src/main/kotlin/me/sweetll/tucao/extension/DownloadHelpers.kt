package me.sweetll.tucao.extension

import android.Manifest
import android.app.Activity
import com.chad.library.adapter.base.entity.MultiItemEntity
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import me.sweetll.tucao.AppApplication
import me.sweetll.tucao.business.download.model.Part
import me.sweetll.tucao.business.download.model.Video
import me.sweetll.tucao.di.service.XmlApiService
import me.sweetll.tucao.model.json.Result
import zlc.season.rxdownload2.RxDownload
import java.util.*
import javax.inject.Inject

object DownloadHelpers {

    private val rxDownload: RxDownload = RxDownload.getInstance().context(AppApplication.get())

    private val serviceInstance = ServiceInstance()

    fun loadDownloadedVideos(): MutableList<MultiItemEntity> {
        val part1 = Part("P1", 1024, 0, 0, mutableListOf())
        val part2 = Part("P2", 1024, 0, 0, mutableListOf())
        val video1 = Video("11", "Video1", "", 2048)
        video1.addSubItem(part1)
        video1.addSubItem(part2)
        val video2 = Video("22", "Video2", "", 1024)
        val part3 = Part("P1", 1024, 0, 0, mutableListOf())
        video2.addSubItem(part3)

        val data = mutableListOf<MultiItemEntity>(video1, video2)

        return data
    }

    fun startDownload(activity: Activity, result: Result) {
        RxPermissions(activity)
                .request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .doOnNext {
                    granted ->
                    if (!granted) {
                        throw RuntimeException("请允许写权限")
                    }
                }
                .flatMap {
                    Observable.fromIterable(result.video)
                }
                .flatMap {
                    video ->
                    serviceInstance.xmlApiService.playUrl(video.type, video.vid, System.currentTimeMillis() / 1000)
                            .subscribeOn(Schedulers.io())
                }
                .flatMap {
                    response ->
                    if ("succ" == response.result) {
                        Observable.just(response.durls)
                    } else {
                        Observable.error(Throwable("请求视频接口出错"))
                    }
                }
                .flatMap {
                    durls ->
                    rxDownload.serviceDownload(durls[0].url, UUID.randomUUID().toString().replace("-", ""), null)
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    "On next".logD()
                }, {
                    error ->
                    error.message?.toast()
                }, {
                    "开始下载".toast()
                })

        rxDownload.serviceDownload("", "", "")
                .subscribe()
    }

    fun pauseDownload(result: Result) {

    }

    fun cancelDownload(result: Result) {

    }


    class ServiceInstance {
        @Inject
        lateinit var xmlApiService: XmlApiService

        init {
            AppApplication.get()
                    .getApiComponent()
                    .inject(this)
        }
    }

}
