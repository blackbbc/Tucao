package me.sweetll.tucao.business.download.model

import android.os.Parcel
import android.os.Parcelable
import com.chad.library.adapter.base.entity.AbstractExpandableItem
import com.chad.library.adapter.base.entity.MultiItemEntity
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import me.sweetll.tucao.business.download.adapter.DownloadedVideoAdapter
import me.sweetll.tucao.rxdownload.entity.DownloadStatus

@JsonIgnoreProperties(ignoreUnknown = true)
class Video(val hid: String,
            val title: String,
            val thumb: String,
            val flag: Int = DownloadStatus.READY,
            var downloadSize: Long = 0L,
            var totalSize: Long = 0L,
            var checkable: Boolean = false,
            var checked: Boolean = false,
            var singlePart: Boolean = false): AbstractExpandableItem<Part>(), MultiItemEntity, Parcelable {
    override fun getLevel(): Int = 0

    override fun getItemType(): Int = DownloadedVideoAdapter.TYPE_VIDEO

    companion object {
        @JvmField val CREATOR: Parcelable.Creator<Video> = object : Parcelable.Creator<Video> {
            override fun createFromParcel(source: Parcel): Video = Video(source)
            override fun newArray(size: Int): Array<Video?> = arrayOfNulls(size)
        }
    }

    constructor(source: Parcel) : this(source.readString(), source.readString(), source.readString(), source.readInt(), source.readLong(), source.readLong(), 1 == source.readInt(), 1 == source.readInt(), 1 == source.readInt())

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeString(hid)
        dest?.writeString(title)
        dest?.writeString(thumb)
        dest?.writeInt(flag)
        dest?.writeLong(downloadSize)
        dest?.writeLong(totalSize)
        dest?.writeInt((if (checkable) 1 else 0))
        dest?.writeInt((if (checked) 1 else 0))
        dest?.writeInt((if (singlePart) 1 else 0))
    }
}
