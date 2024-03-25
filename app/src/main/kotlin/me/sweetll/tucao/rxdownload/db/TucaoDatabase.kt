package me.sweetll.tucao.rxdownload.db

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import me.sweetll.tucao.AppApplication
import me.sweetll.tucao.rxdownload.entity.DownloadBean
import me.sweetll.tucao.rxdownload.entity.DownloadBeanDao
import me.sweetll.tucao.rxdownload.entity.DownloadMission
import me.sweetll.tucao.rxdownload.entity.DownloadMissionDao


@Database(entities = [DownloadBean::class, DownloadMission::class], version = 1)
abstract class TucaoDatabase : RoomDatabase() {
    companion object {
        val db by lazy {
            Room.databaseBuilder(AppApplication.get(), TucaoDatabase::class.java, "tucao_database")
                .build()
        }
    }

    abstract fun downloadDao(): DownloadBeanDao
    abstract fun missionDao(): DownloadMissionDao
}
