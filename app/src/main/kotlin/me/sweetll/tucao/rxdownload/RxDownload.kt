package me.sweetll.tucao.rxdownload

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.IBinder
import io.reactivex.Observable
import me.sweetll.tucao.model.json.Part
import me.sweetll.tucao.rxdownload.entity.DownloadEvent
import me.sweetll.tucao.rxdownload.entity.DownloadMission
import me.sweetll.tucao.rxdownload.function.DownloadService

class RxDownload {

    private lateinit var context: Context

    private var bound = false
    private var downloadService: DownloadService? = null

    private object Holder {
        @SuppressLint("StaticFieldLeak")
        val INSTANCE = RxDownload()
    }

    companion object {
        private val instance: RxDownload by lazy { Holder.INSTANCE }

        fun getInstance(context: Context): RxDownload {
            instance.context = context.applicationContext
            if (!instance.bound) instance.ensureBind().subscribe()
            return instance
        }
    }

    private fun ensureBind(): Observable<DownloadService> {
        if (bound)
            return Observable.just(downloadService)
        else
            return Observable.create {
                emitter ->
                val intent = Intent(context, DownloadService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
                context.bindService(intent, object : ServiceConnection {

                    override fun onServiceConnected(name: ComponentName?, binder: IBinder) {
                        downloadService = (binder as DownloadService.DownloadBinder).getService()
                        bound = true
                        emitter.onNext(downloadService!!)
                        emitter.onComplete()
                    }

                    override fun onServiceDisconnected(name: ComponentName?) {
                        bound = false
                    }

                }, Context.BIND_AUTO_CREATE)
        }
    }

    fun download(mission: DownloadMission, part: Part) {
        ensureBind().subscribe({
            service ->
            service.download(mission, part)
        })
    }

    fun downloadDanmu(url: String, saveName: String, savePath: String) {
        ensureBind().subscribe {
            service ->
            service.downloadDanmu(url, saveName, savePath)
        }
    }

    fun pause(vid: String) {
        ensureBind().subscribe {
            service ->
            service.pause(vid)
        }
    }

    fun cancel(vid: String, delete: Boolean = true) {
        ensureBind().subscribe {
            service ->
            service.cancel(vid, delete)
        }
    }

    fun receive(vid: String): Observable<DownloadEvent> {
        return ensureBind()
                .flatMap {
                    service ->
                    service.receive(vid).toObservable()
                }
    }
}
