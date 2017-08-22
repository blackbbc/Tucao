package me.sweetll.tucao.rxdownload.entity

import com.raizlabs.android.dbflow.annotation.Column
import com.raizlabs.android.dbflow.annotation.PrimaryKey
import com.raizlabs.android.dbflow.annotation.Table
import io.reactivex.disposables.Disposable
import me.sweetll.tucao.di.service.ApiConfig
import me.sweetll.tucao.extension.sumByLong
import me.sweetll.tucao.rxdownload.db.TucaoDatabase

@Table(database = TucaoDatabase::class)
data class DownloadMission(
        @Column var hid: String = "",
        @Column var order: Int = 0,
        @Column var title: String = "",
        @Column var type: String = "",
        @PrimaryKey var vid: String = "") {

    @Column(typeConverter = BeanListConverter::class) var beans: MutableList<DownloadBean> = mutableListOf()

    @Transient var request: Disposable? = null

    var pause: Boolean = true

    val taskName: String
        get() = "$title/p$order"

    val downloadLength: Long
        get() = beans.sumByLong { it.downloadLength }

    val contentLength: Long
        get() = beans.sumByLong { it.contentLength }

}