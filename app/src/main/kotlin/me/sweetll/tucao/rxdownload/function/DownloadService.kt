package me.sweetll.tucao.rxdownload.function

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.support.v4.app.NotificationCompat
import android.support.v4.app.TaskStackBuilder
import android.support.v4.util.ArrayMap
import android.util.Log
import com.raizlabs.android.dbflow.kotlinextensions.*
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.processors.BehaviorProcessor
import io.reactivex.schedulers.Schedulers
import me.sweetll.tucao.R
import me.sweetll.tucao.business.download.DownloadActivity
import me.sweetll.tucao.business.download.model.Part
import me.sweetll.tucao.di.service.ApiConfig
import me.sweetll.tucao.extension.DownloadHelpers
import me.sweetll.tucao.extension.formatWithUnit
import me.sweetll.tucao.extension.toast
import me.sweetll.tucao.rxdownload.entity.DownloadBean
import me.sweetll.tucao.rxdownload.entity.DownloadEvent
import me.sweetll.tucao.rxdownload.entity.DownloadMission
import me.sweetll.tucao.rxdownload.entity.DownloadStatus
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import java.io.BufferedInputStream
import java.nio.channels.FileChannel
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit

class DownloadService: Service() {

    companion object {
        const val ONGOING_NOTIFICATION_ID = 1
        const val COMPLETED_NOTIFICATION_ID = 2

        const val ACTION_PAUSE = "pause"
        const val ACTION_CANCEL = "cancel"
        const val ACTION_URL = "url"
    }

    lateinit var binder: DownloadBinder

    val downloadApi: DownloadApi by lazy {
        Retrofit.Builder()
                .baseUrl(ApiConfig.BASE_RAW_API_URL)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(
                        OkHttpClient.Builder()
                                .addInterceptor(HttpLoggingInterceptor())
                                .build()
                )
                .build()
                .create(DownloadApi::class.java)
    }

    var semaphore: Semaphore = Semaphore(1) // 同时只允许1个任务下载

    val missionMap: ArrayMap<String, DownloadMission> = ArrayMap()
    val processorMap: ArrayMap<String, BehaviorProcessor<DownloadEvent>> = ArrayMap()
    val partMap: ArrayMap<String, Boolean> = ArrayMap()

    override fun onCreate() {
        Log.d("DownloadService", "On Create")
        super.onCreate()
        binder = DownloadBinder()

        syncFromDb()
    }

    // Warning: Not guarantee to call
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
                    val url = it.getStringExtra(ACTION_URL)
                    pause(url)
                }
                ACTION_CANCEL -> {
                    val url = it.getStringExtra(ACTION_URL)
                    DownloadHelpers.cancelDownload(url)
                }
            }
        }
        return START_STICKY
    }

    private fun syncFromDb() {
        val beans = (select from DownloadBean::class).list
        beans.forEach {
            val mission = DownloadMission(it)
            missionMap.put(it.url, mission)
            processorMap.put(it.url, BehaviorProcessor.create<DownloadEvent>()
                    .apply {
                         onNext(DownloadEvent(DownloadStatus.PAUSED, 0, 0)) // 默认暂停状态
                    })
        }
    }

    fun download(url: String, saveName: String, savePath: String, taskName: String, part: Part) {
        // 检查missionMap中是否存在
        val mission = missionMap.getOrPut(url, {
            DownloadMission(
                    DownloadBean(url = url, saveName = saveName, savePath = savePath)
            )
        }).apply { pause = false } // 置为false

        val processor = processorMap.getOrPut(url, {
            BehaviorProcessor.create<DownloadEvent>()
                    .apply {
                        sample(500, java.util.concurrent.TimeUnit.MILLISECONDS)
                                .subscribe {
                                    event ->
                                    consumeEvent(mission, event, part)
                                }
                        partMap.put(url, true)
                    }
        }).apply {
            onNext(DownloadEvent(DownloadStatus.READY))
            if (!partMap.getOrElse(url, {
                partMap.put(url, true)
                false
            })) {
                sample(500, TimeUnit.MILLISECONDS)
                        .subscribe {
                            event ->
                            consumeEvent(mission, event, part)
                        }
            }
        }

        // 开始下载
        Flowable.create<DownloadEvent>({
            // 检查状态
            while (!semaphore.tryAcquire()) {
                if (mission.pause) {
                    processor.onNext(DownloadEvent(DownloadStatus.PAUSED))
                    return@create
                }
            }

            processor.onNext(DownloadEvent(DownloadStatus.STARTED, mission.bean.downloadLength, mission.bean.contentLength, taskName))

            downloadApi.download(mission.bean.url, mission.bean.getRange(), mission.bean.getIfRange())
                    .subscribeOn(Schedulers.io())
                    .doAfterTerminate { semaphore.release() }
                    .subscribe({
                        response ->
                        try {
                            val header = response.headers()
                            val body = response.body()

                            mission.bean.lastModified = header.get("Last-Modified") ?: "Wed, 21 Oct 2015 07:28:00 GMT"
                            mission.bean.etag = header.get("ETag") ?: "\"\""

                            var count: Int
                            val data = ByteArray(1024 * 8)
                            val fileSize: Long = body!!.contentLength()

                            if (response.code() == 200 || mission.bean.downloadLength == 0L) {
                                mission.bean.downloadLength = 0
                                mission.bean.contentLength = fileSize
                                mission.bean.prepareFile()
                            }

                            val inputStream = BufferedInputStream(body.byteStream(), 1024 * 8)
                            val file = mission.bean.getRandomAccessFile()
                            val fileChannel = file.channel
                            val outputStream = fileChannel.map(FileChannel.MapMode.READ_WRITE, mission.bean.downloadLength, fileSize)

                            count = inputStream.read(data)
                            while (count != -1 && !mission.pause) {
                                mission.bean.downloadLength += count
                                outputStream.put(data, 0, count)
                                processor.onNext(DownloadEvent(DownloadStatus.STARTED, mission.bean.downloadLength, mission.bean.contentLength, taskName))

                                count = inputStream.read(data)
                            }

                            if (mission.pause) {
                                processor.onNext(DownloadEvent(DownloadStatus.PAUSED))
                            }

                            if (mission.bean.downloadLength == mission.bean.contentLength) {
                                processor.onNext(DownloadEvent(DownloadStatus.COMPLETED, mission.bean.downloadLength, mission.bean.contentLength, taskName))
                            }

                            fileChannel.close()
                            file.close()
                        } catch (error: Exception) {
                            error.printStackTrace()
                        }
                    }, {
                        error ->
                        error.printStackTrace()
                        processor.onNext(DownloadEvent(DownloadStatus.FAILED))
                    })
        }, BackpressureStrategy.LATEST)
                .subscribeOn(Schedulers.newThread())
                .publish()
                .connect()
    }

    fun pause(url: String) {
        missionMap[url]?.let {
            it.pause = true
        }
    }

    fun cancel(url: String, delete: Boolean) {
        missionMap[url]?.let {
            it.pause = true
            if (delete) {
                it.bean.getFile().delete()
            }
            missionMap.remove(url)
            processorMap.remove(url)
            it.bean.delete()
        }
    }

    fun receive(url: String): BehaviorProcessor<DownloadEvent> {
        val processor = processorMap[url]!! // 可能不存在吗？
        return processor
    }

    private fun consumeEvent(mission: DownloadMission, event: DownloadEvent, part: Part) {
        when (event.status) {
            DownloadStatus.PAUSED -> {
                mission.bean.save()
                stopForeground(true)
            }
            DownloadStatus.COMPLETED -> {
                mission.bean.save()
                part.durls.find {
                    it.url == mission.bean.url
                }?.let {
                    it.flag = DownloadStatus.COMPLETED
                    it.downloadSize = event.downloadSize
                    it.totalSize = event.totalSize
                    part.update()
                }
                DownloadHelpers.saveDownloadPart(part)
            }
        }
        sendNotification(event, mission.bean.url)
    }

    private fun sendNotification(event: DownloadEvent, url: String) {
        when (event.status) {
            DownloadStatus.STARTED -> {
                val nfIntent = Intent(this, DownloadActivity::class.java)
                nfIntent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                nfIntent.action = DownloadActivity.ACTION_DOWNLOADING

                val stackBuilder = TaskStackBuilder.create(this)
                stackBuilder.addParentStack(DownloadActivity::class.java)
                stackBuilder.addNextIntent(nfIntent)

                val pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)

                val pauseIntent = Intent(this, DownloadService::class.java)
                pauseIntent.action = ACTION_PAUSE
                pauseIntent.putExtra(ACTION_URL, url)
                val piPause = PendingIntent.getService(this, 0, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT)

                val cancelIntent = Intent(this, DownloadService::class.java)
                cancelIntent.action = ACTION_CANCEL
                cancelIntent.putExtra(ACTION_URL, url)
                val piCancel = PendingIntent.getService(this, 0, cancelIntent, PendingIntent.FLAG_UPDATE_CURRENT)

                val builder = NotificationCompat.Builder(this)
                        .setProgress(event.totalSize.toInt(), event.downloadSize.toInt(), false)
                        .setSmallIcon(R.drawable.ic_file_download_white)
                        .setContentTitle(event.taskName)
                        .setContentText("${event.downloadSize.formatWithUnit()}/${event.totalSize.formatWithUnit()}")
                        .setContentIntent(pendingIntent)
                        .addAction(R.drawable.ic_action_pause, "暂停", piPause)
                        .addAction(R.drawable.ic_action_cancel, "取消", piCancel)
                val notification = builder.build()
                notification.flags = notification.flags or Notification.FLAG_NO_CLEAR or Notification.FLAG_ONGOING_EVENT

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

                val builder = NotificationCompat.Builder(this)
                        .setProgress(0, 0, false)
                        .setSmallIcon(R.drawable.ic_file_download_white)
                        .setContentTitle(event.taskName)
                        .setContentText("${event.totalSize.formatWithUnit()}/已完成")
                        .setContentIntent(pendingIntent)
                val notification = builder.build()
                notification.flags = notification.flags or Notification.FLAG_AUTO_CANCEL

                val notifyMgr = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

                stopForeground(true)

                notifyMgr.notify(COMPLETED_NOTIFICATION_ID, notification)
            }
        }
    }

    inner class DownloadBinder: Binder() {
        fun getService(): DownloadService = this@DownloadService
    }
}
