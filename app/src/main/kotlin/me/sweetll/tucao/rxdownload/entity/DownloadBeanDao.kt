package me.sweetll.tucao.rxdownload.entity

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface DownloadBeanDao {

    @Query("SELECT * FROM DownloadBean")
    fun getAll(): List<DownloadBean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg beans: DownloadBean)

    @Update
    fun update(bean: DownloadBean)

    @Delete
    fun delete(bean: DownloadBean)
}