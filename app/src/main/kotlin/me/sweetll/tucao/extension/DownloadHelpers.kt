package me.sweetll.tucao.extension

import android.Manifest
import android.app.Activity
import android.os.Environment
import android.os.Environment.DIRECTORY_DOWNLOADS
import com.chad.library.adapter.base.entity.MultiItemEntity
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import me.sweetll.tucao.AppApplication
import me.sweetll.tucao.business.download.event.RefreshDownloadingVideoEvent
import me.sweetll.tucao.model.json.Part
import me.sweetll.tucao.model.json.Video
import me.sweetll.tucao.di.service.XmlApiService
import org.greenrobot.eventbus.EventBus
import javax.inject.Inject
import me.sweetll.tucao.business.download.event.RefreshDownloadedVideoEvent
import java.io.File
import android.preference.PreferenceManager
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import me.sweetll.tucao.di.service.ApiConfig
import me.sweetll.tucao.di.service.RawApiService
import me.sweetll.tucao.rxdownload.RxDownload
import me.sweetll.tucao.rxdownload.entity.DownloadBean
import me.sweetll.tucao.rxdownload.entity.DownloadMission
import me.sweetll.tucao.rxdownload.entity.DownloadStatus
import okhttp3.ResponseBody
import java.io.FileOutputStream

object DownloadHelpers {
    private val DOWNLOAD_FILE_NAME = "download"

    private val KEY_S_DOWNLOAD_VIDEO = "download_video"

    private val defaultPath = Environment.getExternalStoragePublicDirectory(DIRECTORY_DOWNLOADS).path + "/me.sweetll.tucao"
    private val rxDownload: RxDownload = RxDownload.getInstance(AppApplication.get())

    val serviceInstance = ServiceInstance()

    private val adapter by lazy {
        val moshi = Moshi.Builder()
                .build()
        val type = Types.newParameterizedType(MutableList::class.java, Video::class.java)
        moshi.adapter<MutableList<Video>>(type)
    }

    fun getDownloadFolder(): File {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(AppApplication.get())
        val downloadPath = sharedPref.getString("download_path", defaultPath)
        val downloadFolder = File(downloadPath)
        if (!downloadFolder.exists()) {
            downloadFolder.mkdirs()
        }
        return downloadFolder
    }

    fun loadDownloadVideos(): MutableList<Video> {
        val sp = DOWNLOAD_FILE_NAME.getSharedPreference()
        val jsonString = sp.getString(KEY_S_DOWNLOAD_VIDEO, "[]")
        return adapter.fromJson(jsonString)!!
    }

    fun loadDownloadingVideos(): MutableList<MultiItemEntity> = loadDownloadVideos()
            .filter {
                video ->
                video.subItems.any {
                    it.flag != DownloadStatus.COMPLETED
                }
            }
            .map {
                video ->
                video.subItems.removeAll { it.flag == DownloadStatus.COMPLETED }
                video
            }
            .toMutableList()

    fun loadDownloadedVideos(): MutableList<MultiItemEntity> = loadDownloadVideos()
            .filter {
                video ->
                video.subItems.any {
                    it.flag == DownloadStatus.COMPLETED
                }
            }
            .map {
                video ->
                video.subItems.removeAll { it.flag != DownloadStatus.COMPLETED }
                video.totalSize = video.subItems.sumByLong(Part::totalSize)
                video.downloadSize = video.totalSize
                video
            }
            .toMutableList()

    fun saveDownloadVideo(video: Video) {
        val videos = loadDownloadVideos()

        val existVideo = videos.find { it.hid == video.hid }
        if (existVideo != null) {
            existVideo.subItems.add(video.subItems[0])
            existVideo.subItems.sortBy(Part::order)
        } else {
            videos.add(0, video)
        }

        val jsonString = adapter.toJson(videos)
        val sp = DOWNLOAD_FILE_NAME.getSharedPreference()
        sp.edit {
            putString(KEY_S_DOWNLOAD_VIDEO, jsonString)
        }
        EventBus.getDefault().post(RefreshDownloadingVideoEvent())
    }

    // 保存已下载的视频
    fun saveDownloadPart(part: Part) {
        val videos = loadDownloadVideos()
        val existVideo = videos.flatMap {
            it.subItems
        }.find { it.vid == part.vid}
        existVideo?.flag = part.flag
        existVideo?.downloadSize = part.downloadSize
        existVideo?.totalSize = part.totalSize

        val jsonString = adapter.toJson(videos)
        val sp = DOWNLOAD_FILE_NAME.getSharedPreference()
        sp.edit {
            putString(KEY_S_DOWNLOAD_VIDEO, jsonString)
        }
        EventBus.getDefault().post(RefreshDownloadingVideoEvent())
        EventBus.getDefault().post(RefreshDownloadedVideoEvent())
    }

    // 开始下载
    fun startDownload(activity: Activity, video: Video) {
        RxPermissions(activity)
                .request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .doOnNext {
                    granted ->
                    if (!granted) {
                        throw RuntimeException("请给予写存储卡权限以供离线缓存使用")
                    }
                    "已开始下载".toast()
                }
                .flatMap {
                    Observable.fromIterable(video.subItems)
                }
                .subscribe({
                    part ->
                    download(video, part)
                }, {
                    error ->
                    error.printStackTrace()
                    error.message?.toast()
                })
    }

    // 继续下载
    fun resumeDownload(video: Video, part: Part) {
        val mission = DownloadMission(hid = video.hid, order = part.order, title = video.title)
        rxDownload.download(mission, part)
    }

    // 下载新视频
    private fun download(video: Video, part: Part) {
        // 先下载弹幕
        val playerId = ApiConfig.generatePlayerId(video.hid, part.order)
        val saveName = "danmu.xml"
        val savePath = "${getDownloadFolder().absolutePath}/${video.hid}/p${part.order}"
        rxDownload.downloadDanmu("${ApiConfig.DANMU_API_URL}&playerID=$playerId&r=${System.currentTimeMillis() / 1000}", saveName, savePath)

        // 再处理视频
        val mission = DownloadMission(hid = video.hid, order = part.order, title = video.title, type = part.type, vid = part.vid) // 没有beans
        if (part.durls.isNotEmpty()) {
            // 处理直传的情况
            part.durls.forEach {
                durl ->
                mission.beans.add(DownloadBean(durl.url, saveName = "${durl.order}", savePath = "${DownloadHelpers.getDownloadFolder().absolutePath}/${mission.hid}/p${mission.order}"))
            }
        }
        rxDownload.download(mission, part)

        video.parts.add(part)
        saveDownloadVideo(video)
    }

    fun pauseDownload(part: Part) {
        part.durls.forEach {
            rxDownload.pause(it.url)
        }
    }

    fun updateDanmu(parts: List<Part>) {
        val videos = loadDownloadVideos()

        val requests = videos.fold(mutableListOf<Observable<ResponseBody>>()) {
            total, video ->
            video.subItems.filter {
                part ->
                parts.any { it.vid == part.vid }
            }.forEach {
                val playerId = ApiConfig.generatePlayerId(video.hid, it.order)
                val saveName = "danmu.xml"
                val savePath = "${getDownloadFolder().absolutePath}/${video.hid}/p${it.order}"
                val ob = serviceInstance.rawApiService.danmu(playerId, System.currentTimeMillis() / 1000)
                        .doOnNext {
                            responseBody ->
                            val outputFile = File(savePath, saveName)
                            val outputStream = FileOutputStream(outputFile)

                            outputStream.write(responseBody.bytes())
                            outputStream.flush()
                            outputStream.close()
                        }
                total.add(ob)
            }
            total
        }

        "更新弹幕中...".toast()
        Observable.fromIterable(requests)
                .subscribeOn(Schedulers.io())
                .flatMap { it }
                .observeOn(AndroidSchedulers.mainThread())
                .doOnComplete {
                    "更新弹幕成功".toast()
                    "更新弹幕成功".logD()
                }
                .subscribe ({
                    // Do nothing
                }, {
                    error ->
                    error.printStackTrace()
                    "更新弹幕失败".toast()
                })
    }

    fun cancelDownload(parts: List<Part>) {
        val videos = loadDownloadVideos()
        videos.forEach {
            video ->
            video.subItems.removeAll {
                part ->
                parts.any { it.vid == part.vid }
            }
        }
        videos.removeAll {
            it.subItems.isEmpty()
        }

        val jsonString = adapter.toJson(videos)
        val sp = DOWNLOAD_FILE_NAME.getSharedPreference()
        sp.edit {
            putString(KEY_S_DOWNLOAD_VIDEO, jsonString)
        }

        parts.forEach {
            part ->
            part.durls.forEach {
                rxDownload.cancel(it.url, true)
            }
        }
        EventBus.getDefault().post(RefreshDownloadingVideoEvent())
        EventBus.getDefault().post(RefreshDownloadedVideoEvent())
    }

    fun cancelDownload(url: String) {
        val videos = loadDownloadVideos()

        videos.flatMap {
            it.subItems
        }.find {
            it.durls.any { it.url == url }
        }?.let {
            cancelDownload(listOf(it))
        }
    }

    interface Callback {
        fun startDownload()

        fun pauseDownload()
    }


    class ServiceInstance {
        @Inject
        lateinit var xmlApiService: XmlApiService

        @Inject
        lateinit var rawApiService: RawApiService

        init {
            AppApplication.get()
                    .getApiComponent()
                    .inject(this)
        }
    }

}
