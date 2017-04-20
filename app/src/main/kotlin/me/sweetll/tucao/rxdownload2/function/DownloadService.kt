package me.sweetll.tucao.rxdownload2.function

import android.app.IntentService
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.processors.BehaviorProcessor
import io.reactivex.schedulers.Schedulers
import me.sweetll.tucao.rxdownload2.entity.DownloadBean
import me.sweetll.tucao.rxdownload2.entity.DownloadEvent
import me.sweetll.tucao.rxdownload2.entity.DownloadMission
import me.sweetll.tucao.rxdownload2.entity.DownloadStatus
import org.reactivestreams.Processor
import java.util.concurrent.Semaphore

class DownloadService: IntentService("DownloadWorker") {
    lateinit var binder: DownloadBinder

    lateinit var downloadApi: DownloadApi

    lateinit var semaphore: Semaphore

    lateinit var missionMap: MutableMap<String, DownloadMission>
    lateinit var processorMap: MutableMap<String, BehaviorProcessor<DownloadEvent>>

    init {
        setIntentRedelivery(true) // Make sure service restart when die
    }

    override fun onCreate() {
        super.onCreate()
        binder = DownloadBinder()

        semaphore = Semaphore(1) // 同时只允许一个任务下载

        // TODO: 从数据库同步状态
        missionMap = mutableMapOf()
    }

    override fun onDestroy() {
        super.onDestroy()
        // TODO: 同步状态到数据库
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    override fun onHandleIntent(intent: Intent?) {

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

            semaphore.acquire()
            processor.onNext(DownloadEvent(DownloadStatus.STARTED))

            downloadApi.download(mission.bean.url, mission.bean.getRange(), mission.bean.getIfRange())
                    .subscribeOn(Schedulers.io())
                    .doAfterTerminate { semaphore.release() }
                    .subscribe({
                        body ->

                        processor.onComplete()
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
