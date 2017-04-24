package me.sweetll.tucao.rxdownload2

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.processors.BehaviorProcessor
import me.sweetll.tucao.rxdownload2.entity.DownloadEvent
import me.sweetll.tucao.rxdownload2.function.DownloadService

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
                emitter.onNext(downloadService)
                emitter.onComplete()
            } else {
                val intent = Intent(context, DownloadService::class.java)
                context.bindService(intent, object : ServiceConnection {
                    override fun onServiceConnected(name: ComponentName?, binder: IBinder) {
                        downloadService = (binder as DownloadService.DownloadBinder).getService()
                        bound = true
                        emitter.onNext(downloadService)
                        emitter.onComplete()
                    }

                    override fun onServiceDisconnected(name: ComponentName?) {
                        bound = false
                    }
                }, Context.BIND_AUTO_CREATE)
            }
        }
    }

    fun download(url: String, saveName: String, savePath: String) {
        ensureBind().subscribe({
            service ->
            service.download(url, saveName, savePath)
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
            service.cancel(url)
        }
    }

//    fun receive(url: String): Observable<BehaviorProcessor<DownloadEvent>> {
//        return ensureBind()
//                .map {
//                    service ->
//                    service.receive(url)
//                }
//    }

    fun receive(url: String): Observable<DownloadEvent> {
        return ensureBind()
                .flatMap {
                    service ->
                    service.receive(url).toObservable()
                }
    }
}
