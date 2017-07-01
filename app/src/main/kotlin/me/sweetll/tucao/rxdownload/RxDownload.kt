package me.sweetll.tucao.rxdownload

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import io.reactivex.Observable
import me.sweetll.tucao.business.download.model.Part
import me.sweetll.tucao.rxdownload.entity.DownloadEvent
import me.sweetll.tucao.rxdownload.function.DownloadService

class RxDownload {

    private lateinit var context: Context

    private var bound = false
    private var downloadService: DownloadService? = null

    private object Holder {
        val INSTANCE = RxDownload()
    }

    companion object {
        private val instance: RxDownload by lazy { Holder.INSTANCE }

        fun getInstance(context: Context): RxDownload {
            instance.context = context.applicationContext
            return instance
        }

    }

    private fun ensureBind(): Observable<DownloadService> {
        return Observable.create {
            emitter ->
            if (bound) {
                emitter.onNext(downloadService!!)
                emitter.onComplete()
            } else {
                val intent = Intent(context, DownloadService::class.java)
                context.startService(intent)
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
    }

    fun download(url: String, saveName: String, savePath: String, taskName: String, part: Part) {
        ensureBind().subscribe({
            service ->
            service.download(url, saveName, savePath, taskName, part)
        })
    }

    fun pause(url: String) {
        ensureBind().subscribe {
            service ->
            service.pause(url)
        }
    }

    fun cancel(url: String, delete: Boolean = true) {
        ensureBind().subscribe {
            service ->
            service.cancel(url, delete)
        }
    }

    fun receive(url: String): Observable<DownloadEvent> {
        return ensureBind()
                .flatMap {
                    service ->
                    service.receive(url).toObservable()
                }
    }
}
