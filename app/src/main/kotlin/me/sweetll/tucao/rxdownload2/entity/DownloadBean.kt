package me.sweetll.tucao.rxdownload2.entity

data class DownloadBean(val url: String,
                        val etag: String,
                        val lastModified: String,
                        val contentLength: Long,
                        val downloadLength: Long,
                        val saveName: String,
                        val savePath: String)
