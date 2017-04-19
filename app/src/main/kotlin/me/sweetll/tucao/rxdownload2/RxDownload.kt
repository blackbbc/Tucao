package me.sweetll.tucao.rxdownload2

import android.content.Context

class RxDownload() {

    private lateinit var context: Context

    private var bound = false

    private object Holder {
        val INSTANCE = RxDownload()
    }

    companion object {
        private val instance: RxDownload by lazy { Holder.INSTANCE }

        fun getInstance(context: Context): RxDownload {
            instance.context = context.applicationContext
            return instance
        }

        fun download(url: String) {

        }

        fun pause(url: String) {

        }

        fun cancel(url: String) {

        }

        fun receive(url: String) {

        }
    }
}
