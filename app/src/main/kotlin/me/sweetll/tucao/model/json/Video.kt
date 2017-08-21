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
            var checked: Boolean = false,
            var singlePart: Boolean = false) : IExpandable<Part>, MultiItemEntity, Parcelable {

    var video: MutableList<Part> = mutableListOf()

    var parts: MutableList<Part>
        get() = video
        set(value) {
            video = value
        }

    private var expandable = false

    override fun getLevel(): Int = 0

    override fun getItemType(): Int = DownloadedVideoAdapter.TYPE_VIDEO

    override fun setExpanded(expanded: Boolean) {
        this.expandable = expandable
    }

    override fun getSubItems(): MutableList<Part> = video

    override fun isExpanded(): Boolean = expandable

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
            1 == source.readInt(),
            1 == source.readInt()
    ) {
        source.readList(video, List::class.java.classLoader)
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
        writeInt((if (singlePart) 1 else 0))
        writeList(video)
    }

    companion object {
        @JvmField val CREATOR: Parcelable.Creator<Video> = object : Parcelable.Creator<Video> {
            override fun createFromParcel(source: Parcel): Video = Video(source)
            override fun newArray(size: Int): Array<Video?> = arrayOfNulls(size)
        }
    }
}
