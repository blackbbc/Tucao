package me.sweetll.tucao.rxdownload.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import io.reactivex.disposables.Disposable
import java.io.File
import java.io.RandomAccessFile

@Entity(tableName = "DownloadBean")
data class DownloadBean(
    @PrimaryKey var url: String = "",
    var etag: String = "\"\"",
    var lastModified: String = "Wed, 21 Oct 2015 07:28:00 GMT",
    var contentLength: Long = 0L,
    var downloadLength: Long = 0L,
    var saveName: String = "",
    var savePath: String = ""
) {

    @Transient var connecting = false
    @Transient var request: Disposable? = null

    fun cancelIfConnecting() {
        if (connecting) request?.dispose()
    }

    fun getRange(): String = "bytes=$downloadLength-"

    fun getIfRange(): String? {
        // Notice: ETag is not support by some *fucking* server. So we use last-modified here.
        return lastModified
    }

    fun getFile(): File {
        val file = File(savePath, saveName)
        if (!file.exists()) {
            file.parentFile.mkdirs()
            file.createNewFile()
        }
        return file
    }

    fun getRandomAccessFile(): RandomAccessFile {
        return RandomAccessFile(getFile(), "rw")
    }

    // TODO: 检查可用空间
    fun prepareFile(): Boolean {
        getRandomAccessFile().setLength(contentLength)
        return true
    }
}