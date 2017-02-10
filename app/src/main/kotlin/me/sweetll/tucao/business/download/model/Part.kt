package me.sweetll.tucao.business.download.model

import android.os.Parcel
import android.os.Parcelable
import com.chad.library.adapter.base.entity.MultiItemEntity
import me.sweetll.tucao.business.download.adapter.DownloadedVideoAdapter
import me.sweetll.tucao.extension.sumByLong
import me.sweetll.tucao.model.xml.Durl
import zlc.season.rxdownload2.entity.DownloadFlag
import zlc.season.rxdownload2.entity.DownloadStatus

class Part(val title: String,
           val order: Int,
           val vid: String = "",
           val type: String = "",
           var flag: Int = DownloadFlag.NORMAL,
           var status: DownloadStatus = DownloadStatus(),
           val durls: MutableList<Durl> = mutableListOf(),
           var checkable: Boolean = false,
           var checked: Boolean = false,
           var hasPlay: Boolean = false,
           var stateController: StateController? = null): MultiItemEntity, Parcelable{
    override fun getItemType(): Int = DownloadedVideoAdapter.TYPE_PART

    fun update() {
        status.totalSize = durls.sumByLong { it.status.totalSize }
        status.downloadSize = durls.sumByLong { it.status.downloadSize }
        val flags = durls.map(Durl::flag)
        if (flags.any { it == DownloadFlag.FAILED }) {
            flag = DownloadFlag.FAILED
        } else if (flags.any { it == DownloadFlag.PAUSED }) {
            flag = DownloadFlag.PAUSED
        } else if (flags.any { it == DownloadFlag.STARTED }) {
            flag = DownloadFlag.STARTED
        } else if (flags.any { it == DownloadFlag.WAITING }) {
            flag = DownloadFlag.WAITING
        } else if (flags.any { it == DownloadFlag.NORMAL }) {
            flag = DownloadFlag.NORMAL
        } else if (flags.all { it == DownloadFlag.COMPLETED }) {
            flag = DownloadFlag.COMPLETED
        }
    }

    fun copy() = Part(title, order, vid, type, flag, status, durls, checkable, checked, hasPlay)

    companion object {
        @JvmField val CREATOR: Parcelable.Creator<Part> = object : Parcelable.Creator<Part> {
            override fun createFromParcel(source: Parcel): Part = Part(source)
            override fun newArray(size: Int): Array<Part?> = arrayOfNulls(size)
        }
    }

    constructor(source: Parcel) : this(source.readString(), source.readInt(), source.readString(), source.readString(), source.readInt(), source.readParcelable<DownloadStatus>(DownloadStatus::class.java.classLoader), source.createTypedArrayList(Durl.CREATOR), 1 == source.readInt(), 1 == source.readInt(), 1 == source.readInt())

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeString(title)
        dest?.writeInt(order)
        dest?.writeString(vid)
        dest?.writeString(type)
        dest?.writeInt(flag)
        dest?.writeParcelable(status, 0)
        dest?.writeTypedList(durls)
        dest?.writeInt((if (checkable) 1 else 0))
        dest?.writeInt((if (checked) 1 else 0))
        dest?.writeInt((if (hasPlay) 1 else 0))
    }
}
