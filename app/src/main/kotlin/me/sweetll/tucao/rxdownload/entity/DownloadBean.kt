package me.sweetll.tucao.rxdownload.entity

import com.raizlabs.android.dbflow.annotation.Column
import com.raizlabs.android.dbflow.annotation.PrimaryKey
import com.raizlabs.android.dbflow.annotation.Table
import me.sweetll.tucao.rxdownload.db.TucaoDatabase
import java.io.File
import java.io.RandomAccessFile

@Table(database = TucaoDatabase::class)
data class DownloadBean(@PrimaryKey var url: String = "",
                        @Column var etag: String = "",
                        @Column var lastModified: String = "Wed, 21 Oct 2015 07:28:00 GMT",
                        @Column var contentLength: Long = 0L,
                        @Column var downloadLength: Long = 0L,
                        @Column var saveName: String = "",
                        @Column var savePath: String = "") {
    fun getRange(): String {
        return "bytes=$downloadLength-"
    }

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