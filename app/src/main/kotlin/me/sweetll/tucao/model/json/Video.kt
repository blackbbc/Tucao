package me.sweetll.tucao.model.json

import android.os.Parcel
import android.os.Parcelable
import com.chad.library.adapter.base.entity.IExpandable
import com.chad.library.adapter.base.entity.MultiItemEntity
import com.squareup.moshi.Json
import me.sweetll.tucao.business.download.adapter.DownloadedVideoAdapter
import me.sweetll.tucao.rxdownload.entity.DownloadStatus

data class Video(val hid: String = "",
            val title: String = "",
            val play: Int = 0,
            val mukio: Int = 0,
            val create: String = "",
            val thumb: String = "",
            val typeid: Int = 0,
            val typename: String = "",
            val description: String = "",
            val userid: String = "",
            val user: String = "",
            val keywords: String = "",
            val part: Int = 0,
            val flag: Int = DownloadStatus.READY,
            var downloadSize: Long = 0L,
            var totalSize: Long = 0L,
            var checkable: Boolean = false,
            var checked: Boolean = false) : IExpandable<Part>, MultiItemEntity, Parcelable {

    var video: MutableList<Part> = mutableListOf()

    var parts: MutableList<Part>
        get() = video
        set(value) {
            video = value
        }

    val singlePart: Boolean
        get() = part == 1

    private var expanded = false

    override fun getLevel(): Int = 0

    override fun getItemType(): Int = DownloadedVideoAdapter.TYPE_VIDEO

    override fun setExpanded(expanded: Boolean) {
        this.expanded = expanded
    }

    override fun getSubItems(): MutableList<Part> = video

    override fun isExpanded(): Boolean = expanded

    constructor(source: Parcel) : this(
            source.readString(),
            source.readString(),
            source.readInt(),
            source.readInt(),
            source.readString(),
            source.readString(),
            source.readInt(),
            source.readString(),
            source.readString(),
            source.readString(),
            source.readString(),
            source.readString(),
            source.readInt(),
            source.readInt(),
            source.readLong(),
            source.readLong(),
            1 == source.readInt(),
            1 == source.readInt()
    ) {
        video = source.createTypedArrayList(Part.CREATOR)
    }

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeString(hid)
        writeString(title)
        writeInt(play)
        writeInt(mukio)
        writeString(create)
        writeString(thumb)
        writeInt(typeid)
        writeString(typename)
        writeString(description)
        writeString(userid)
        writeString(user)
        writeString(keywords)
        writeInt(part)
        writeInt(flag)
        writeLong(downloadSize)
        writeLong(totalSize)
        writeInt((if (checkable) 1 else 0))
        writeInt((if (checked) 1 else 0))
        writeTypedList(video)
    }

    companion object {
        @JvmField val CREATOR: Parcelable.Creator<Video> = object : Parcelable.Creator<Video> {
            override fun createFromParcel(source: Parcel): Video = Video(source)
            override fun newArray(size: Int): Array<Video?> = arrayOfNulls(size)
        }
    }
}
