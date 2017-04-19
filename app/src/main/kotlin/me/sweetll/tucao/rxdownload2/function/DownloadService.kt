package me.sweetll.tucao.rxdownload2.function

import android.app.IntentService
import android.content.Intent
import android.os.Binder
import android.os.IBinder

class DownloadService: IntentService("DownloadWorker") {
    lateinit var binder: DownloadBinder

    init {
        setIntentRedelivery(true) // Make sure service restart when die
    }

    override fun onCreate() {
        super.onCreate()
        binder = DownloadBinder()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    override fun onHandleIntent(intent: Intent?) {

    }

    inner class DownloadBinder: Binder() {
        fun getService(): DownloadService = this@DownloadService
    }
}
