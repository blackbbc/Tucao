package me.sweetll.tucao.rxdownload.entity

import com.raizlabs.android.dbflow.annotation.PrimaryKey
import com.raizlabs.android.dbflow.annotation.Table
import io.reactivex.disposables.Disposable
import me.sweetll.tucao.rxdownload.db.TucaoDatabase
import java.io.File
import java.io.RandomAccessFile

@Table(database = TucaoDatabase::class)
data class DownloadBean(@PrimaryKey var url: String = "",
                        @PrimaryKey var etag: String = "\"\"",
                        @PrimaryKey var lastModified: String = "Wed, 21 Oct 2015 07:28:00 GMT",
                        @PrimaryKey var contentLength: Long = 0L,
                        @PrimaryKey var downloadLength: Long = 0L,
                        @PrimaryKey var saveName: String = "",
                        @PrimaryKey var savePath: String = "") {

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
        val file =  File(savePath, saveName)
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