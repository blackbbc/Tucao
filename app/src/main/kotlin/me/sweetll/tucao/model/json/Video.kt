package me.sweetll.tucao.model.json

import android.os.Parcel
import android.os.Parcelable
import me.sweetll.tucao.model.xml.Durl
import zlc.season.rxdownload2.entity.DownloadFlag
import zlc.season.rxdownload2.entity.DownloadStatus

data class Video(val title: String,
                 val type: String,
                 val vid: String = "",
                 var checked: Boolean = false,
                 var order: Int = 0,
                 var flag: Int = DownloadFlag.NORMAL,
                 var status: DownloadStatus = DownloadStatus(),
                 var durls: MutableList<Durl> = mutableListOf()) : Parcelable {
    companion object {
        @JvmField val CREATOR: Parcelable.Creator<Video> = object : Parcelable.Creator<Video> {
            override fun createFromParcel(source: Parcel): Video = Video(source)
            override fun newArray(size: Int): Array<Video?> = arrayOfNulls(size)
        }
    }

    constructor(source: Parcel) : this(source.readString(), source.readString(), source.readString(), 1 == source.readInt(), source.readInt(), source.readInt(), source.readParcelable<DownloadStatus>(DownloadStatus::class.java.classLoader), source.createTypedArrayList(Durl.CREATOR))

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeString(title)
        dest?.writeString(type)
        dest?.writeString(vid)
        dest?.writeInt((if (checked) 1 else 0))
        dest?.writeInt(order)
        dest?.writeInt(flag)
        dest?.writeParcelable(status, 0)
        dest?.writeTypedList(durls)
    }
}
