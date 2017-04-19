package me.sweetll.tucao.rxdownload2.entity

object DownloadStatus {
    const val READY: Int = 1 shl 0 // 等待中?
    const val STARTED: Int = 1 shl 1
    const val PAUSED: Int = 1 shl 2
    const val COMPLETED: Int = 1 shl 3
    const val FAILED: Int = 1 shl 4
}
