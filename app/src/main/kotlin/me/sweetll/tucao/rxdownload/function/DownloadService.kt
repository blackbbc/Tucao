package me.sweetll.tucao.rxdownload.function

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import androidx.collection.ArrayMap
import androidx.core.app.NotificationCompat
import androidx.core.app.TaskStackBuilder
import dagger.android.AndroidInjection
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.processors.BehaviorProcessor
import io.reactivex.schedulers.Schedulers
import me.sweetll.tucao.AppApplication.Companion.PRIMARY_CHANNEL
import me.sweetll.tucao.R
import me.sweetll.tucao.business.download.DownloadActivity
import me.sweetll.tucao.extension.DownloadHelpers
import me.sweetll.tucao.extension.formatWithUnit
import me.sweetll.tucao.extension.logD
import me.sweetll.tucao.model.json.Part
import me.sweetll.tucao.model.xml.Durl
import me.sweetll.tucao.rxdownload.db.TucaoDatabase
import me.sweetll.tucao.rxdownload.entity.DownloadBean
import me.sweetll.tucao.rxdownload.entity.DownloadEvent
import me.sweetll.tucao.rxdownload.entity.DownloadMission
import me.sweetll.tucao.rxdownload.entity.DownloadStatus
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class DownloadService : Service() {

    companion object {
        const val ONGOING_NOTIFICATION_ID = 1
        const val COMPLETED_NOTIFICATION_ID = 2
        const val FAILED_NOTIFICATION_ID = 3

        const val ACTION_PAUSE = "pause"
        const val ACTION_CANCEL = "cancel"
        const val ACTION_URL = "url"
    }

    val notifyMgr by lazy {
        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    lateinit var binder: DownloadBinder

    @Inject
    lateinit var downloadApi: DownloadApi

    var semaphore: Semaphore = Semaphore(1) // 同时只允许1个任务下载

    val missionMap: ArrayMap<String, DownloadMission> = ArrayMap()
    val processorMap: ArrayMap<String, BehaviorProcessor<DownloadEvent>> = ArrayMap()
    val sampleMap: ArrayMap<String, Boolean> = ArrayMap()
    val missionQueue = LinkedBlockingQueue<String>()

    override fun onCreate() {
        Log.d("DownloadService", "On Create")
        super.onCreate()
        binder = DownloadBinder()

        AndroidInjection.inject(this)

        syncFromDb()
        launchMissionConsumer()
    }

    override fun onDestroy() {
        Log.d("DownloadService", "On Destroy")
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("DownloadService", "On Start Command")
        intent?.let {
            when (it.action) {
                ACTION_PAUSE -> {
                    val vid = it.getStringExtra(ACTION_URL)
                    pause(vid)
                }
                ACTION_CANCEL -> {
                    val vid = it.getStringExtra(ACTION_URL)
                    DownloadHelpers.cancelDownload(vid)
                }
            }
        }
        return START_STICKY
    }

    @SuppressLint("CheckResult")
    private fun syncFromDb() {
        Observable.create<List<DownloadMission>> {
            val missions = TucaoDatabase.db.missionDao().getAll()
            it.onNext(missions)
            it.onComplete()
        }.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { missions ->
                missions.forEach {
                    missionMap.put(it.vid, it)
                    processorMap.put(it.vid, BehaviorProcessor.create<DownloadEvent>()
                        .apply {
                            onNext(DownloadEvent(DownloadStatus.PAUSED)) // 默认暂停状态
                        })
                }
            }
    }

    private fun launchMissionConsumer() {
        Flowable.create<DownloadEvent>({

            while (true) {

                while (!semaphore.tryAcquire(1, TimeUnit.SECONDS)) {
                    // Wait for semaphore...
                }

                // 开始下载
                val vid = missionQueue.take()
                val mission = missionMap[vid]!!
                val processor = processorMap[vid]!!

                if (mission.beans.isEmpty()) {
                    // 尚未获取到下载地址
                    doObtainUrl(mission, processor)
                } else {
                    // 已获取到下载地址
                    doDownload(mission, processor)
                }

            }

        }, BackpressureStrategy.LATEST)
                .subscribeOn(Schedulers.newThread())
                .publish()
                .connect()
    }

    private fun doObtainUrl(mission: DownloadMission, processor: BehaviorProcessor<DownloadEvent>) {
        processor.onNext(DownloadEvent(DownloadStatus.OBTAIN_URL))
        mission.request = DownloadHelpers.serviceInstance.xmlApiService.playUrl(mission.type, mission.vid, System.currentTimeMillis() / 1000)
                .subscribeOn(Schedulers.io())
                .doOnDispose {
                    processor.onNext(DownloadEvent(DownloadStatus.PAUSED))
                    mission.request = null
                    semaphore.release()
                }
                .doAfterTerminate {
                    mission.request = null
                }
                .flatMap {
                    response ->
                    if ("succ" == response.result) {
                        Observable.just(response.durls)
                    } else {
                        Observable.error(Throwable("请求视频接口出错"))
                    }
                }
                .subscribe({
                    durls ->
                    durls.forEach {
                        durl ->
                        mission.beans.add(DownloadBean(durl.url, saveName = "${durl.order}", savePath = "${DownloadHelpers.getDownloadFolder().absolutePath}/${mission.hid}/p${mission.order}"))
                    }
                    doDownload(mission, processor)
                }, {
                    error ->
                    error.printStackTrace()
                    processor.onNext(DownloadEvent(DownloadStatus.FAILED))
                })
    }

    private fun doDownload(mission: DownloadMission, processor: BehaviorProcessor<DownloadEvent>) {
        processor.onNext(DownloadEvent(DownloadStatus.CONNECTING))
        mission.pause = false
        mission.beans.forEach {
            bean ->
            bean.request = downloadApi.download(bean.url, bean.getRange(), bean.getIfRange())
                    .subscribeOn(Schedulers.io())
                    .doAfterTerminate {
                        bean.request = null
                        if (mission.beans.all { it.request == null }) {
                            semaphore.release()
                        }
                    }
                    .doOnDispose {
                        processor.onNext(DownloadEvent(DownloadStatus.PAUSED))
                        bean.request = null
                        if (mission.beans.all { it.request == null }) {
                            semaphore.release()
                        }
                    }
                    .doOnSubscribe { bean.connecting = true }
                    .doOnNext { bean.connecting = false }
                    .subscribe({
                        response ->
                        try {
                            val header = response.headers()
                            val body = response.body()

                            bean.lastModified = header.get("Last-Modified") ?: "Wed, 21 Oct 2015 07:28:00 GMT"
                            bean.etag = header.get("ETag") ?: "\"\""

                            var count: Int
                            val data = ByteArray(1024 * 8)
                            val fileSize: Long = body!!.contentLength()

                            if (response.code() == 200 || bean.downloadLength == 0L) {
                                bean.downloadLength = 0
                                bean.contentLength = fileSize
                                bean.prepareFile()
                            }

                            val inputStream = BufferedInputStream(body.byteStream(), 1024 * 8)
                            val file = bean.getRandomAccessFile()
                            file.seek(bean.downloadLength)

                            processor.onNext(DownloadEvent(DownloadStatus.STARTED, mission.downloadLength, mission.contentLength))

                            count = inputStream.read(data)
                            while (count != -1 && !mission.pause) {
                                bean.downloadLength += count
                                file.write(data, 0, count)
                                processor.onNext(DownloadEvent(DownloadStatus.STARTED, mission.downloadLength, mission.contentLength))
                                count = inputStream.read(data)
                            }

                            if (mission.pause) {
                                processor.onNext(DownloadEvent(DownloadStatus.PAUSED))
                            }

                            if (mission.downloadLength == mission.contentLength) {
                                processor.onNext(DownloadEvent(DownloadStatus.COMPLETED, mission.downloadLength, mission.contentLength))
                            }

                            file.close()
                        } catch (error: Exception) {
                            error.printStackTrace()
                            processor.onNext(DownloadEvent(DownloadStatus.FAILED))
                        }
                    }, {
                        error ->
                        error.printStackTrace()
                        processor.onNext(DownloadEvent(DownloadStatus.FAILED))
                    })
        }
    }

    fun download(newMission: DownloadMission, part: Part) {
        val mission = missionMap.getOrPut(newMission.vid, {
            newMission.apply { exec { insert(newMission) } }
        })

        processorMap.getOrPut(mission.vid, {
            BehaviorProcessor.create<DownloadEvent>().apply {
                sample(500, TimeUnit.MILLISECONDS)
                        .subscribe {
                            consumeEvent(mission, it, part)
                        }
                sampleMap.put(mission.vid, true)
            }
        }).apply {
            onNext(DownloadEvent(DownloadStatus.READY))
            if (!sampleMap.getOrElse(mission.vid, { false })) {
                sampleMap.put(mission.vid, true)
                sample(500, TimeUnit.MILLISECONDS)
                        .subscribe {
                            consumeEvent(mission, it, part)
                        }
            }
        }

        missionQueue.put(mission.vid)
    }

    fun downloadDanmu(url: String, saveName: String, savePath: String) {
        downloadApi.downloadDanmu(url)
                .subscribeOn(Schedulers.io())
                .subscribe({
                    responseBody ->
                    val outputFile = File(savePath, saveName)
                    if (!outputFile.exists()) outputFile.parentFile.mkdirs()
                    val outputStream = FileOutputStream(outputFile)

                    outputStream.write(responseBody.bytes())
                    outputStream.flush()
                    outputStream.close()
                }, {
                    error ->
                    error.printStackTrace()
                })
    }

    fun pause(vid: String) {
        if (missionQueue.remove(vid)) {
            processorMap[vid]?.onNext(DownloadEvent(DownloadStatus.PAUSED))
        }
        missionMap[vid]?.let {
            mission ->
            mission.pause = true
            if (mission.beans.isEmpty()) {
                // 还在请求下载地址
                mission.request?.dispose()
            } else {
                mission.beans.forEach {
                    it.cancelIfConnecting()
                }
            }
        }
    }

    fun cancel(vid: String, delete: Boolean) {
        missionQueue.remove(vid)
        missionMap[vid]?.let {
            mission ->
            mission.pause = true
            if (delete) {
                if (mission.beans.isEmpty()) {
                    mission.request?.dispose()
                } else {
                    mission.beans.forEach {
                        it.cancelIfConnecting()
                        it.getFile().delete()
                    }
                }
            }
            missionMap.remove(vid)
            processorMap.remove(vid)
            mission.exec { delete(mission) }
        }
    }

    fun receive(vid: String): BehaviorProcessor<DownloadEvent> {
        val processor = processorMap[vid]!! // 可能不存在吗？
        return processor
    }

    private fun consumeEvent(mission: DownloadMission, event: DownloadEvent, part: Part) {
        when (event.status) {
            DownloadStatus.PAUSED -> {
                mission.exec { update(mission) }
                stopForeground(true)
            }
            DownloadStatus.FAILED -> {
                mission.pause = true
                mission.exec { update(mission) }
            }
            DownloadStatus.COMPLETED -> {
                mission.exec { update(mission) }
                mission.beans.forEach {
                    bean ->
                    part.durls.add(Durl(flag = DownloadStatus.COMPLETED, cacheFileName = bean.saveName, cacheFolderPath = bean.savePath, downloadSize = bean.downloadLength, totalSize = bean.contentLength))
                }
                part.update()
                DownloadHelpers.saveDownloadPart(part)
            }
        }
        sendNotification(mission, event)
    }

    private fun sendNotification(mission: DownloadMission, event: DownloadEvent) {
        "Send Notification ${event.status}...".logD()
        when (event.status) {
            DownloadStatus.OBTAIN_URL, DownloadStatus.CONNECTING, DownloadStatus.STARTED -> {
                val nfIntent = Intent(this, DownloadActivity::class.java)
                nfIntent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                nfIntent.action = DownloadActivity.ACTION_DOWNLOADING

                val stackBuilder = TaskStackBuilder.create(this)
                stackBuilder.addParentStack(DownloadActivity::class.java)
                stackBuilder.addNextIntent(nfIntent)

                val pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)

                val pauseIntent = Intent(this, DownloadService::class.java)
                pauseIntent.action = ACTION_PAUSE
                pauseIntent.putExtra(ACTION_URL, mission.vid)
                val piPause = PendingIntent.getService(this, 0, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT)

                val cancelIntent = Intent(this, DownloadService::class.java)
                cancelIntent.action = ACTION_CANCEL
                cancelIntent.putExtra(ACTION_URL, mission.vid)
                val piCancel = PendingIntent.getService(this, 0, cancelIntent, PendingIntent.FLAG_UPDATE_CURRENT)

                val builder = NotificationCompat.Builder(this, PRIMARY_CHANNEL)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(mission.taskName)
                        .setContentIntent(pendingIntent)
                        .addAction(R.drawable.ic_action_pause, "暂停", piPause)
                        .addAction(R.drawable.ic_action_cancel, "取消", piCancel)
                when (event.status) {
                    DownloadStatus.OBTAIN_URL -> builder.setContentText("获取下载地址中...")
                    DownloadStatus.CONNECTING -> builder.setContentText("连接中...")
                    DownloadStatus.STARTED -> {
                        // 下载中
                        builder.setProgress(event.totalSize.toInt(), event.downloadSize.toInt(), false)
                                .setContentText("${event.downloadSize.formatWithUnit()}/${event.totalSize.formatWithUnit()}")
                    }
                }
                val notification = builder.build()
                notification.flags = notification.flags or Notification.FLAG_NO_CLEAR

                startForeground(ONGOING_NOTIFICATION_ID, notification)
            }
            DownloadStatus.COMPLETED -> {
                val nfIntent = Intent(this, DownloadActivity::class.java)
                nfIntent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                nfIntent.action = DownloadActivity.ACTION_DOWNLOADED

                val stackBuilder = TaskStackBuilder.create(this)
                stackBuilder.addParentStack(DownloadActivity::class.java)
                stackBuilder.addNextIntent(nfIntent)

                val pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)

                val builder = NotificationCompat.Builder(this, PRIMARY_CHANNEL)
                        .setProgress(0, 0, false)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(mission.taskName)
                        .setContentText("${event.totalSize.formatWithUnit()}/已完成")
                        .setContentIntent(pendingIntent)
                val notification = builder.build()
                notification.flags = notification.flags or Notification.FLAG_AUTO_CANCEL

                stopForeground(true)

                notifyMgr.notify(COMPLETED_NOTIFICATION_ID, notification)
            }
            DownloadStatus.FAILED -> {
                val nfIntent = Intent(this, DownloadActivity::class.java)
                nfIntent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                nfIntent.action = DownloadActivity.ACTION_DOWNLOADING

                val stackBuilder = TaskStackBuilder.create(this)
                stackBuilder.addParentStack(DownloadActivity::class.java)
                stackBuilder.addNextIntent(nfIntent)

                val pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)

                val builder = NotificationCompat.Builder(this, PRIMARY_CHANNEL)
                        .setProgress(0, 0, false)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(mission.taskName)
                        .setContentText("下载失败")
                        .setContentIntent(pendingIntent)
                val notification = builder.build()
                notification.flags = notification.flags or Notification.FLAG_AUTO_CANCEL

                stopForeground(true)

                notifyMgr.notify(FAILED_NOTIFICATION_ID, notification)
            }
        }
    }

    inner class DownloadBinder : Binder() {
        fun getService(): DownloadService = this@DownloadService
    }
}
