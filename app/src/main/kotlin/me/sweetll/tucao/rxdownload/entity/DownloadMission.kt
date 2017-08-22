package me.sweetll.tucao.rxdownload.entity

import com.raizlabs.android.dbflow.annotation.Column
import com.raizlabs.android.dbflow.annotation.PrimaryKey
import com.raizlabs.android.dbflow.annotation.Table
import me.sweetll.tucao.di.service.ApiConfig
import me.sweetll.tucao.extension.sumByLong
import me.sweetll.tucao.rxdownload.db.TucaoDatabase

@Table(database = TucaoDatabase::class)
data class DownloadMission(
        @Column var hid: String = "",
        @Column var order: Int = 0,
        @Column var title: String = "") {

    @PrimaryKey var playerId: String = ApiConfig.generatePlayerId(hid, order)

    @Column(typeConverter = BeanListConverter::class) var beans: MutableList<DownloadBean> = mutableListOf()

    var pause: Boolean
        get() = beans.all { it.pause }
        set(value) {
            beans.forEach { it.pause = value }
        }

    val taskName: String
        get() = "$title/p$order"

    val downloadLength: Long
        get() = beans.sumByLong { it.downloadLength }

    val contentLength: Long
        get() = beans.sumByLong { it.contentLength }

}