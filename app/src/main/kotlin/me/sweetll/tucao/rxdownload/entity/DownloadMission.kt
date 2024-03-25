package me.sweetll.tucao.rxdownload.entity

import android.annotation.SuppressLint
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import me.sweetll.tucao.extension.sumByLong
import me.sweetll.tucao.rxdownload.db.TucaoDatabase


@Entity(tableName = "DownloadMission")
data class DownloadMission(
    var hid: String = "",
    var order: Int = 0,
    var title: String = "",
    var type: String = "",
    @PrimaryKey var vid: String = ""
) {

    @TypeConverters(BeanListConverter::class)
    var beans: MutableList<DownloadBean> = mutableListOf()

    @Transient
    var request: Disposable? = null

    var pause: Boolean = true

    val taskName: String
        get() = "$title/p$order"

    val downloadLength: Long
        get() = beans.sumByLong { it.downloadLength }

    val contentLength: Long
        get() = beans.sumByLong { it.contentLength }

    @SuppressLint("CheckResult")
    fun exec(execs: (DownloadMissionDao.() -> Unit)) {
        Observable.create<Any> {
            TucaoDatabase.db.missionDao().execs()
        }.subscribeOn(Schedulers.io())
    }

}