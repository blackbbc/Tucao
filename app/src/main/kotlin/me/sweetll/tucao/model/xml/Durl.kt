package me.sweetll.tucao.model.xml

import android.os.Parcel
import android.os.Parcelable
import org.simpleframework.xml.Element
import org.simpleframework.xml.Root
import me.sweetll.tucao.rxdownload.entity.DownloadStatus

@Root(name = "durl", strict = false)
data class Durl(
        @field:Element(name = "order") var order: Int = 0,
        @field:Element(name = "length") var length: Long = 0L,
        @field:Element(name = "url") var url: String = "",
        var cacheFolderPath: String = "",
        var cacheFileName: String = "",
        var flag: Int = DownloadStatus.READY,
        var downloadSize: Long = 0L,
        var totalSize: Long = 0L
) : Parcelable {

    fun getCacheAbsolutePath(): String = "$cacheFolderPath/$cacheFileName"

    companion object {
        @JvmField val CREATOR: Parcelable.Creator<Durl> = object : Parcelable.Creator<Durl> {
            override fun createFromParcel(source: Parcel): Durl = Durl(source)
            override fun newArray(size: Int): Array<Durl?> = arrayOfNulls(size)
        }
    }

    constructor(source: Parcel) : this(source.readInt(), source.readLong(), source.readString(), source.readString(), source.readString(), source.readInt(), source.readLong(), source.readLong())

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeInt(order)
        dest?.writeLong(length)
        dest?.writeString(url)
        dest?.writeString(cacheFolderPath)
        dest?.writeString(cacheFileName)
        dest?.writeInt(flag)
        dest?.writeLong(downloadSize)
        dest?.writeLong(totalSize)
    }
}
