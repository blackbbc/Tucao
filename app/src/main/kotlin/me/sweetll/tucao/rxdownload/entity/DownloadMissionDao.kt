package me.sweetll.tucao.rxdownload.entity

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface DownloadMissionDao {

    @Query("SELECT * FROM DownloadMission")
    fun getAll(): List<DownloadMission>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg beans: DownloadMission)

    @Update
    fun update(bean: DownloadMission)

    @Delete
    fun delete(bean: DownloadMission)
}