package me.sweetll.tucao.rxdownload.function

import android.app.IntentService
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.support.v4.app.NotificationCompat
import android.support.v4.app.TaskStackBuilder
import com.raizlabs.android.dbflow.kotlinextensions.*
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.processors.BehaviorProcessor
import io.reactivex.schedulers.Schedulers
import me.sweetll.tucao.R
import me.sweetll.tucao.business.download.DownloadActivity
import me.sweetll.tucao.di.service.ApiConfig
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

class DownloadService: IntentService("DownloadWorker") {

    companion object {
        const val ONGOING_NOTIFICATION_ID = 1
        const val COMPLETED_NOTIFICATION_ID = 2
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
        }).apply { pause = false }

        val processor = processorMap.getOrPut(url, {
            BehaviorProcessor.create<DownloadEvent>()
                    .apply {
                        onNext(DownloadEvent(DownloadStatus.READY))
                        sample(100, java.util.concurrent.TimeUnit.MILLISECONDS)
                                .subscribe {
                                    // 默认是啥?
                                    event ->
                                    sendNotification(event)
                                }
                    }
        })

        // 开始下载
        Flowable.create<DownloadEvent>({
            // 检查状态
            while (!semaphore.tryAcquire()) {
                if (mission.pause) {
                    processor.onNext(DownloadEvent(DownloadStatus.PAUSED))
                    return@create
                }
            }

            processor.onNext(DownloadEvent(DownloadStatus.STARTED, mission.bean.downloadLength, mission.bean.contentLength))

            downloadApi.download(mission.bean.url, mission.bean.getRange(), mission.bean.getIfRange())
                    .subscribeOn(Schedulers.io())
                    .doAfterTerminate { semaphore.release() }
                    .subscribe({
                        response ->
                        try {
                            val header = response.headers()
                            val body = response.body()

                            mission.bean.lastModified = header.get("Last-Modified")
                            mission.bean.etag = header.get("ETag")

                            var count: Int
                            val data = ByteArray(1024 * 8)
                            val fileSize: Long = body.contentLength()

                            if (response.code() == 200) {
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
                                processor.onNext(DownloadEvent(DownloadStatus.STARTED, mission.bean.downloadLength, mission.bean.contentLength))

                                count = inputStream.read(data)
                            }

                            if (mission.pause) {
                                processor.onNext(DownloadEvent(DownloadStatus.PAUSED))
                            }

                            if (mission.bean.downloadLength == mission.bean.contentLength) {
                                processor.onNext(DownloadEvent(DownloadStatus.COMPLETED, mission.bean.downloadLength, mission.bean.contentLength))
                            }

                            fileChannel.close()
                            file.close()
                        } catch (error: Exception) {
                            error.printStackTrace()
                        } finally {
                            semaphore.release()
                        }
                    }, {
                        error ->
                        error.printStackTrace()
                        processor.onNext(DownloadEvent(DownloadStatus.FAILED))
                        semaphore.release()
                    })
        }, BackpressureStrategy.LATEST)
                .publish()
                .connect()
    }

    fun pause(url: String) {
        missionMap[url]?.let {
            it.pause = true
        }
    }

    fun cancel(url: String) {
        missionMap[url]?.let {
            it.pause = true
            it.bean.getFile().delete()
            missionMap.remove(url)
            processorMap.remove(url)
        }
    }

    fun receive(url: String): BehaviorProcessor<DownloadEvent> {
        val mission = missionMap[url]!! // 可能不存在吗？
        val processor = processorMap.getOrPut(url, {
            BehaviorProcessor.create<DownloadEvent>()
                    .apply {
                        onNext(DownloadEvent(DownloadStatus.PAUSED, mission.bean.downloadLength, mission.bean.contentLength)) // 默认暂停状态
                        sample(100, java.util.concurrent.TimeUnit.MILLISECONDS)
                                .subscribe {
                                    event ->
                                    sendNotification(event)
                                }
                    }
        })
        return processor
    }

    private fun sendNotification(event: DownloadEvent) {
        when (event.status) {
            DownloadStatus.STARTED -> {
                val nfIntent = Intent(this, DownloadActivity::class.java)

                val stackBuilder = TaskStackBuilder.create(this)
                stackBuilder.addParentStack(DownloadActivity::class.java)
                stackBuilder.addNextIntent(nfIntent)

                val pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)

                val builder = NotificationCompat.Builder(this)
                        .setProgress(event.totalSize.toInt(), event.downloadSize.toInt(), false)
                        .setSmallIcon(R.drawable.ic_file_download_white)
                        .setContentTitle("4个任务下载中")
                        .setContentText("缘之空/P1")
                        .setContentIntent(pendingIntent)
                val notification = builder.build()
                notification.flags = notification.flags or Notification.FLAG_NO_CLEAR or Notification.FLAG_ONGOING_EVENT

                val notifyMgr = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notifyMgr.notify(ONGOING_NOTIFICATION_ID, notification)
            }
            DownloadStatus.COMPLETED -> {
                val nfIntent = Intent(this, DownloadActivity::class.java)

                val stackBuilder = TaskStackBuilder.create(this)
                stackBuilder.addParentStack(DownloadActivity::class.java)
                stackBuilder.addNextIntent(nfIntent)

                val pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)

                val builder = NotificationCompat.Builder(this)
                        .setProgress(0, 0, false)
                        .setSmallIcon(R.drawable.ic_file_download_white)
                        .setContentTitle("缘之空/P1")
                        .setContentText("7.9MB/已完成")
                        .setContentIntent(pendingIntent)
                val notification = builder.build()

                val notifyMgr = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

                // TODO: 检查是否还有任务
                notifyMgr.cancel(ONGOING_NOTIFICATION_ID)

                notifyMgr.notify(COMPLETED_NOTIFICATION_ID, notification)
            }
        }
    }

    inner class DownloadBinder: Binder() {
        fun getService(): DownloadService = this@DownloadService
    }
}
