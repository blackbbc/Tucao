package me.sweetll.tucao.rxdownload2.function

import android.app.IntentService
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import com.raizlabs.android.dbflow.kotlinextensions.*
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.processors.BehaviorProcessor
import io.reactivex.schedulers.Schedulers
import me.sweetll.tucao.rxdownload2.entity.DownloadBean
import me.sweetll.tucao.rxdownload2.entity.DownloadEvent
import me.sweetll.tucao.rxdownload2.entity.DownloadMission
import me.sweetll.tucao.rxdownload2.entity.DownloadStatus
import java.io.BufferedInputStream
import java.io.FileOutputStream
import java.nio.channels.FileChannel
import java.util.concurrent.Semaphore

class DownloadService: IntentService("DownloadWorker") {
    lateinit var binder: DownloadBinder

    lateinit var downloadApi: DownloadApi

    var semaphore: Semaphore = Semaphore(1) // 同时只允许1个任务下载

    val missionMap: MutableMap<String, DownloadMission> = mutableMapOf()
    val processorMap: MutableMap<String, BehaviorProcessor<DownloadEvent>> = mutableMapOf()

    init {
        setIntentRedelivery(true) // Make sure service restart when die
    }

    override fun onCreate() {
        super.onCreate()
        binder = DownloadBinder()

        syncFromDb()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopAllMission()
        syncToDb()
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    override fun onHandleIntent(intent: Intent?) {

    }

    private fun stopAllMission() {
        missionMap.forEach {
            _, mission ->
            mission.pause = true
        }
    }

    private fun syncFromDb() {
        val beans = (select from DownloadBean::class).list
        beans.forEach {
            missionMap.put(it.url, DownloadMission(it))
        }
    }

    private fun syncToDb() {
        missionMap.forEach {
            _, mission ->
            mission.save()
        }
    }

    fun download(url: String, saveName: String, savePath: String) {
        // 检查missionMap中是否存在
        val mission = missionMap.getOrPut(url, {
            DownloadMission(
                    DownloadBean(url = url, saveName = saveName, savePath = savePath)
            )
        })

        val processor = BehaviorProcessor.create<DownloadEvent>()

        // 开始下载
        Flowable.create<DownloadEvent>({
            // 检查状态

            processor.onNext(DownloadEvent(DownloadStatus.READY))

            while (!semaphore.tryAcquire()) {
                if (mission.pause) {
                    processor.onNext(DownloadEvent(DownloadStatus.PAUSED))
                    return@create
                }
            }

            processor.onNext(DownloadEvent(DownloadStatus.STARTED))

            downloadApi.download(mission.bean.url, mission.bean.getRange(), mission.bean.getIfRange())
                    .subscribeOn(Schedulers.io())
                    .doAfterTerminate { semaphore.release() }
                    .subscribe({
                        response ->
                        val header = response.headers()
                        val body = response.body()

                        mission.bean.lastModified = header.get("Last-Modified")
                        mission.bean.etag = header.get("ETag")

                        // TODO: 随机存储

                        var count: Int
                        val data = ByteArray(1024 * 8)
                        val fileSize: Long = body.contentLength()
                        mission.bean.contentLength = fileSize
                        mission.bean.prepareFile()

                        val inputStream = BufferedInputStream(body.byteStream(), 1024 * 8)
                        val file = mission.bean.getRandomAccessFile()
                        val fileChannel = file.channel
                        val outputStream = fileChannel.map(FileChannel.MapMode.READ_WRITE, mission.bean.downloadLength, mission.bean.contentLength - mission.bean.downloadLength)

                        count = inputStream.read(data)
                        while (count != -1 && !mission.pause) {
                            mission.bean.downloadLength += count
                            outputStream.put(data, 0, count)
                            processor.onNext(DownloadEvent(DownloadStatus.STARTED))

                            count = inputStream.read(data)
                        }

                        if (mission.pause) {
                            processor.onNext(DownloadEvent(DownloadStatus.PAUSED))
                        }

                        fileChannel.close()
                        file.close()
                    }, {
                        error ->
                        processor.onError(error)
                    })
        }, BackpressureStrategy.LATEST)
                .publish()
                .connect()

        processorMap.put(url, processor)
    }

    fun pause(url: String) {
        missionMap[url]?.let {
            it.pause = true
        }
    }

    fun cancel(url: String) {
        missionMap[url]?.let {
            it.pause = true
            // TODO: 删除文件
        }
    }

    fun receive(url: String): BehaviorProcessor<DownloadEvent> {
        val processor = processorMap.getValue(url)
        return processor
    }

    inner class DownloadBinder: Binder() {
        fun getService(): DownloadService = this@DownloadService
    }
}
