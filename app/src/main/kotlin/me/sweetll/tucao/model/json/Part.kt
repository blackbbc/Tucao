package me.sweetll.tucao.model.json

import android.os.Parcel
import android.os.Parcelable
import com.chad.library.adapter.base.entity.MultiItemEntity
import me.sweetll.tucao.business.download.adapter.DownloadedVideoAdapter
import me.sweetll.tucao.di.service.ApiConfig
import me.sweetll.tucao.extension.sumByLong
import me.sweetll.tucao.model.xml.Durl
import me.sweetll.tucao.rxdownload.entity.DownloadStatus

data class Part(val title: String = "",
           var order: Int = 0,
           var vid: String = "",
           val type: String = "",
           var flag: Int = DownloadStatus.READY,
           var downloadSize: Long = 0,
           var totalSize: Long = 0,
           var durls: MutableList<Durl> = mutableListOf(),
           var file: String = "",
           var checkable: Boolean = false,
           var checked: Boolean = false,
           var hadPlay: Boolean = false,
           var lastPlayPosition: Int = 0,
           @Transient var stateController: StateController? = null) : MultiItemEntity, Parcelable {
    override fun getItemType(): Int = DownloadedVideoAdapter.TYPE_PART

    fun update() {
        totalSize = durls.sumByLong(Durl::totalSize)
        downloadSize = durls.sumByLong(Durl::downloadSize)

        val flags = durls.map(Durl::flag)
        if (flags.any { it == DownloadStatus.FAILED }) {
            flag = DownloadStatus.FAILED
        } else if (flags.any { it == DownloadStatus.PAUSED }) {
            flag = DownloadStatus.PAUSED
        } else if (flags.any { it == DownloadStatus.STARTED }) {
            flag = DownloadStatus.STARTED
        } else if (flags.any { it == DownloadStatus.READY }) {
            flag = DownloadStatus.READY
        } else if (flags.all { it == DownloadStatus.COMPLETED }) {
            flag = DownloadStatus.COMPLETED
        }
    }

    fun checkDownload(): Boolean = flag != DownloadStatus.READY

    constructor(source: Parcel) : this(
            source.readString(),
            source.readInt(),
            source.readString(),
            source.readString(),
            source.readInt(),
            source.readLong(),
            source.readLong(),
            source.createTypedArrayList(Durl.CREATOR),
            source.readString(),
            1 == source.readInt(),
            1 == source.readInt(),
            1 == source.readInt(),
            source.readInt()
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeString(title)
        writeInt(order)
        writeString(vid)
        writeString(type)
        writeInt(flag)
        writeLong(downloadSize)
        writeLong(totalSize)
        writeTypedList(durls)
        writeString(file)
        writeInt((if (checkable) 1 else 0))
        writeInt((if (checked) 1 else 0))
        writeInt((if (hadPlay) 1 else 0))
        writeInt(lastPlayPosition)
    }

    companion object {
        @JvmField val CREATOR: Parcelable.Creator<Part> = object : Parcelable.Creator<Part> {
            override fun createFromParcel(source: Parcel): Part = Part(source)
            override fun newArray(size: Int): Array<Part?> = arrayOfNulls(size)
        }
    }
}
