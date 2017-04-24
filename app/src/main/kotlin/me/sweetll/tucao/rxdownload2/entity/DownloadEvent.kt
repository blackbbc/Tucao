package me.sweetll.tucao.rxdownload2.entity

data class DownloadEvent(var status: Int, var downloadSize: Long = 0L, var totalSize: Long = 0L)
