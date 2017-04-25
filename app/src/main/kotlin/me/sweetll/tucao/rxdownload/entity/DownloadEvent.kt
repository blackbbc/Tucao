package me.sweetll.tucao.rxdownload.entity

data class DownloadEvent(var status: Int, var downloadSize: Long = 0L, var totalSize: Long = 0L)
