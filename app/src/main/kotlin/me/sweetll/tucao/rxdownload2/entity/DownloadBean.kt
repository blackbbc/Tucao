package me.sweetll.tucao.rxdownload2.entity

data class DownloadBean(val url: String = "",
                        var etag: String = "",
                        var lastModified: String = "",
                        var contentLength: Long = 0L,
                        var downloadLength: Long = 0L,
                        val saveName: String = "",
                        val savePath: String = "") {
    fun getRange(): String {
        return ""
    }

    fun getIfRange(): String {
        return ""
    }
}