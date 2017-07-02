package me.sweetll.tucao.business.download.model

import android.os.Parcel
import android.os.Parcelable
import com.chad.library.adapter.base.entity.MultiItemEntity
import me.sweetll.tucao.business.download.adapter.DownloadedVideoAdapter
import me.sweetll.tucao.extension.sumByLong
import me.sweetll.tucao.model.xml.Durl
import me.sweetll.tucao.rxdownload.entity.DownloadStatus

class Part(val title: String,
           val order: Int,
           var vid: String = "",
           val type: String = "",
           var flag: Int = DownloadStatus.READY,
           var downloadSize: Long = 0,
           var totalSize: Long = 0,
           val durls: MutableList<Durl> = mutableListOf(),
           var checkable: Boolean = false,
           var checked: Boolean = false,
           var hasPlay: Boolean = false,
           var lastPlayPosition: Int = 0,
           @field:Transient var stateController: StateController? = null): MultiItemEntity, Parcelable{
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

    fun checkDownload(): Boolean = durls.isNotEmpty() && durls[0].cacheFileName.isNotEmpty()

    fun copy() = Part(title, order, vid, type, flag, downloadSize, totalSize, durls, checkable, checked, hasPlay)

    companion object {
        @JvmField val CREATOR: Parcelable.Creator<Part> = object : Parcelable.Creator<Part> {
            override fun createFromParcel(source: Parcel): Part = Part(source)
            override fun newArray(size: Int): Array<Part?> = arrayOfNulls(size)
        }
    }

    constructor(source: Parcel) : this(source.readString(), source.readInt(), source.readString(), source.readString(), source.readInt(), source.readLong(), source.readLong(), source.createTypedArrayList(Durl.CREATOR), 1 == source.readInt(), 1 == source.readInt(), 1 == source.readInt(), source.readInt())

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeString(title)
        dest?.writeInt(order)
        dest?.writeString(vid)
        dest?.writeString(type)
        dest?.writeInt(flag)
        dest?.writeLong(downloadSize)
        dest?.writeLong(totalSize)
        dest?.writeTypedList(durls)
        dest?.writeInt((if (checkable) 1 else 0))
        dest?.writeInt((if (checked) 1 else 0))
        dest?.writeInt((if (hasPlay) 1 else 0))
        dest?.writeInt(lastPlayPosition)
    }
}
