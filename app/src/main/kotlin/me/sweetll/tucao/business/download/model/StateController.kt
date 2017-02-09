package me.sweetll.tucao.business.download.model

import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import me.sweetll.tucao.R
import me.sweetll.tucao.extension.DownloadHelpers
import me.sweetll.tucao.model.xml.Durl
import zlc.season.rxdownload2.entity.DownloadEvent
import zlc.season.rxdownload2.entity.DownloadFlag
import zlc.season.rxdownload2.entity.DownloadStatus

class StateController(val sizeText: TextView, val statusImg: ImageView, val progressBar: ProgressBar) {
    var state: DownloadState = Normal()

    fun setEvent(downloadEvent: DownloadEvent) {
        state = when (downloadEvent.flag) {
            DownloadFlag.NORMAL -> Normal()
            DownloadFlag.WAITING -> Waiting()
            DownloadFlag.STARTED -> Started()
            DownloadFlag.PAUSED -> Paused()
            DownloadFlag.FAILED -> Failed()
            DownloadFlag.CANCELED -> Canceled()
            DownloadFlag.DELETED -> Deleted()
            else -> Completed()
        }
        state.setStatus(downloadEvent.downloadStatus)
    }

    fun handleClick(callback: DownloadHelpers.Callback) {
        state.handleClick(callback)
    }

    inner abstract class DownloadState {
        init {
            statusImg.visibility = View.VISIBLE
        }

        abstract fun handleClick(callback: DownloadHelpers.Callback)
        open fun setStatus(status: DownloadStatus) {
            progressBar.max = status.totalSize.toInt()
            progressBar.progress = status.downloadSize.toInt()
        }
    }

    inner class Normal : DownloadState() {
        init {
            sizeText.text = "下载"
            statusImg.setImageResource(R.drawable.ic_file_download_black)
            progressBar.visibility = View.GONE
        }

        override fun handleClick(callback: DownloadHelpers.Callback) {
            callback.startDownload()
        }
    }

    inner class Waiting : DownloadState() {
        init {
            sizeText.text = "等待中"
            statusImg.setImageResource(R.drawable.ic_pause)
            progressBar.visibility = View.GONE
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

        override fun setStatus(status: DownloadStatus) {
            super.setStatus(status)
            sizeText.text = status.formatStatusString
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

    inner class Canceled : DownloadState() {
        init {
            sizeText.text = "下载已取消"
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

        }
    }

    inner class Deleted : DownloadState() {
        init {
            sizeText.text = "下载已取消"
            statusImg.setImageResource(R.drawable.ic_file_download_black)
            progressBar.visibility = View.GONE
        }

        override fun handleClick(callback: DownloadHelpers.Callback) {
            callback.startDownload()
        }
    }

}
