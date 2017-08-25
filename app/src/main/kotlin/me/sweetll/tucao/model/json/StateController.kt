package me.sweetll.tucao.model.json

import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import me.sweetll.tucao.R
import me.sweetll.tucao.extension.DownloadHelpers
import me.sweetll.tucao.extension.formatWithUnit
import me.sweetll.tucao.rxdownload.entity.DownloadEvent
import me.sweetll.tucao.rxdownload.entity.DownloadStatus

class StateController(val sizeText: TextView, val statusImg: ImageView, val progressBar: ProgressBar) {
    var state: DownloadState = UNKNOWN()

    fun setEvent(downloadEvent: DownloadEvent) {
        state = when (downloadEvent.status) {
            DownloadStatus.READY -> READY()
            DownloadStatus.OBTAIN_URL -> OBTAIN_URL()
            DownloadStatus.CONNECTING -> CONNECTING()
            DownloadStatus.STARTED -> Started()
            DownloadStatus.PAUSED -> Paused()
            DownloadStatus.FAILED -> Failed()
            else -> Completed()
        }
        state.setProgress(downloadEvent.downloadSize, downloadEvent.totalSize)
    }

    fun handleClick(callback: DownloadHelpers.Callback) {
        state.handleClick(callback)
    }

    inner abstract class DownloadState {
        init {
            statusImg.visibility = View.VISIBLE
        }

        abstract fun handleClick(callback: DownloadHelpers.Callback)

        open fun setProgress(downloadSize: Long, totalSize: Long) {
            progressBar.max = totalSize.toInt()
            progressBar.progress = downloadSize.toInt()
        }
    }

    inner class UNKNOWN : DownloadState() {
        init {
            sizeText.text = ""
            statusImg.setImageDrawable(null)
            progressBar.visibility = View.GONE
        }

        override fun handleClick(callback: DownloadHelpers.Callback) {
            callback.pauseDownload()
        }
    }


    inner class READY : DownloadState() {
        init {
            sizeText.text = "等待中"
            statusImg.setImageResource(R.drawable.ic_pause)
            progressBar.visibility = View.GONE
        }

        override fun handleClick(callback: DownloadHelpers.Callback) {
            callback.pauseDownload()
        }
    }

    inner class OBTAIN_URL : DownloadState() {
        init {
            sizeText.text = "获取下载地址中..."
            statusImg.setImageResource(R.drawable.ic_pause)
            progressBar.visibility = View.GONE
        }

        override fun handleClick(callback: DownloadHelpers.Callback) {
            callback.pauseDownload()
        }
    }

    inner class CONNECTING : DownloadState() {
        init {
            sizeText.text = "连接中..."
            statusImg.setImageResource(R.drawable.ic_pause)
            progressBar.visibility = View.VISIBLE
        }

        override fun handleClick(callback: DownloadHelpers.Callback) {
            callback.pauseDownload()
        }
    }


    inner class Started : DownloadState() {
        init {
            sizeText.text = ""
            statusImg.setImageResource(R.drawable.ic_pause)
            progressBar.visibility = View.VISIBLE
        }

        override fun handleClick(callback: DownloadHelpers.Callback) {
            callback.pauseDownload()
        }

        override fun setProgress(downloadSize: Long, totalSize: Long) {
            super.setProgress(downloadSize, totalSize)
            sizeText.text = "${downloadSize.formatWithUnit()}/${totalSize.formatWithUnit()}"
        }
    }

    inner class Paused : DownloadState() {
        init {
            sizeText.text = "继续"
            statusImg.setImageResource(R.drawable.ic_file_download_black)
            progressBar.visibility = View.GONE
        }

        override fun handleClick(callback: DownloadHelpers.Callback) {
            callback.startDownload()
        }
    }

    inner class Failed : DownloadState() {
        init {
            sizeText.text = "下载失败"
            statusImg.setImageResource(R.drawable.ic_file_download_black)
            progressBar.visibility = View.GONE
        }

        override fun handleClick(callback: DownloadHelpers.Callback) {
            callback.startDownload()
        }
    }

    inner class Completed : DownloadState() {
        init {
            sizeText.text = "完成"
            statusImg.setImageResource(R.drawable.ic_file_download_black)
            progressBar.visibility = View.GONE
        }

        override fun handleClick(callback: DownloadHelpers.Callback) {
            // 永远都不应该进来！
        }
    }

}
