package me.sweetll.tucao.rxdownload2.entity

import com.raizlabs.android.dbflow.annotation.Column
import com.raizlabs.android.dbflow.annotation.PrimaryKey
import com.raizlabs.android.dbflow.annotation.Table
import me.sweetll.tucao.rxdownload2.db.TucaoDatabase
import java.io.File
import java.io.RandomAccessFile

@Table(database = TucaoDatabase::class)
data class DownloadBean(@PrimaryKey var url: String = "",
                        @Column var etag: String = "",
                        @Column var lastModified: String = "",
                        @Column var contentLength: Long = 0L,
                        @Column var downloadLength: Long = 0L,
                        @Column var saveName: String = "",
                        @Column var savePath: String = "") {
    fun getRange(): String {
        return "bytes=$downloadLength-"
    }

    fun getIfRange(): String? {
        if (etag.isNotEmpty()) {
            return etag
        } else if (lastModified.isNotEmpty()) {
            return lastModified
        } else {
            return null
        }
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