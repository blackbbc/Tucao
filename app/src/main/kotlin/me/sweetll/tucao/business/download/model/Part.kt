package me.sweetll.tucao.business.download.model

import android.os.Parcel
import android.os.Parcelable
import com.chad.library.adapter.base.entity.MultiItemEntity
import me.sweetll.tucao.business.download.adapter.DownloadedVideoAdapter
import me.sweetll.tucao.model.xml.Durl
import zlc.season.rxdownload2.entity.DownloadFlag
import zlc.season.rxdownload2.entity.DownloadStatus

class Part(val title: String,
           val order: Int,
           var flag: Int = DownloadFlag.NORMAL,
           var status: DownloadStatus = DownloadStatus(),
           val durls: MutableList<Durl> = mutableListOf(),
           val checkable: Boolean = false,
           val checked: Boolean = false,
           var stateController: StateController? = null): MultiItemEntity, Parcelable {
    override fun getItemType(): Int = DownloadedVideoAdapter.TYPE_PART

    companion object {
        @JvmField val CREATOR: Parcelable.Creator<Part> = object : Parcelable.Creator<Part> {
            override fun createFromParcel(source: Parcel): Part = Part(source)
            override fun newArray(size: Int): Array<Part?> = arrayOfNulls(size)
        }
    }

    constructor(source: Parcel) : this(source.readString(), source.readInt(), source.readInt(), source.readParcelable<DownloadStatus>(DownloadStatus::class.java.classLoader), source.createTypedArrayList(Durl.CREATOR), 1 == source.readInt(), 1 == source.readInt())

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeString(title)
        dest?.writeInt(order)
        dest?.writeInt(flag)
        dest?.writeParcelable(status, 0)
        dest?.writeTypedList(durls)
        dest?.writeInt((if (checkable) 1 else 0))
        dest?.writeInt((if (checked) 1 else 0))
    }
}
/*
class Part(val title: String,
           val order: Int,
           var flag: Int = DownloadFlag.NORMAL,
           var status: DownloadStatus = DownloadStatus(),
           val durls: MutableList<Durl> = mutableListOf(),
           val checkable: Boolean = false,
           val checked: Boolean = false): MultiItemEntity, Parcelable {
    override fun getItemType(): Int = DownloadedVideoAdapter.TYPE_PART

    companion object {
        @JvmField val CREATOR: Parcelable.Creator<Part> = object : Parcelable.Creator<Part> {
            override fun createFromParcel(source: Parcel): Part = Part(source)
            override fun newArray(size: Int): Array<Part?> = arrayOfNulls(size)
        }
    }

    constructor(source: Parcel) : this(source.readString(), source.readInt(), source.readInt(), source.readParcelable<DownloadStatus>(DownloadStatus::class.java.classLoader), source.createTypedArrayList(Durl.CREATOR), 1 == source.readInt(), 1 == source.readInt())

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeString(title)
        dest?.writeInt(order)
        dest?.writeInt(flag)
        dest?.writeParcelable(status, 0)
        dest?.writeTypedList(durls)
        dest?.writeInt((if (checkable) 1 else 0))
        dest?.writeInt((if (checked) 1 else 0))
    }
}
*/
