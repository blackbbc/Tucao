package me.sweetll.tucao.rxdownload2.entity

import com.raizlabs.android.dbflow.annotation.Column
import com.raizlabs.android.dbflow.annotation.PrimaryKey
import com.raizlabs.android.dbflow.annotation.Table
import me.sweetll.tucao.rxdownload2.db.TucaoDatabase

@Table(database = TucaoDatabase::class)
data class DownloadBean(@PrimaryKey val url: String = "",
                        @Column var etag: String = "",
                        @Column var lastModified: String = "",
                        @Column var contentLength: Long = 0L,
                        @Column var downloadLength: Long = 0L,
                        @Column val saveName: String = "",
                        @Column val savePath: String = "") {
    fun getRange(): String {
        return ""
    }

    fun getIfRange(): String {
        return ""
    }
}