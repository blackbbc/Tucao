package me.sweetll.tucao.rxdownload.entity

object DownloadStatus {
    const val READY: Int = 1 shl 0      // 等待中
    const val OBTAIN_URL: Int = 1 shl 1 // 获取下载地址中
    const val CONNECTING: Int = 1 shl 2 // 连接中
    const val STARTED: Int = 1 shl 3    // 下载中
    const val PAUSED: Int = 1 shl 4     // 已暂停
    const val COMPLETED: Int = 1 shl 5  // 已完成
    const val FAILED: Int = 1 shl 6     // 失败
}
