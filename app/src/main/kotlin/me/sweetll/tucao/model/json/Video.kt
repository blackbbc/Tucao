package me.sweetll.tucao.model.json

import android.os.Parcel
import android.os.Parcelable
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import me.sweetll.tucao.model.xml.Durl

@JsonIgnoreProperties(ignoreUnknown = true)
data class Video(val title: String = "",
                 val type: String = "",
                 var vid: String = "",
                 var checked: Boolean = false,
                 var order: Int = 0,
                 var durls: MutableList<Durl> = mutableListOf(),
                 var file: String = "") : Parcelable {
    companion object {
        @JvmField val CREATOR: Parcelable.Creator<Video> = object : Parcelable.Creator<Video> {
            override fun createFromParcel(source: Parcel): Video = Video(source)
            override fun newArray(size: Int): Array<Video?> = arrayOfNulls(size)
        }
    }

    constructor(source: Parcel) : this(source.readString(), source.readString(), source.readString(), 1 == source.readInt(), source.readInt(), source.createTypedArrayList(Durl.CREATOR), source.readString())

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeString(title)
        dest?.writeString(type)
        dest?.writeString(vid)
        dest?.writeInt((if (checked) 1 else 0))
        dest?.writeInt(order)
        dest?.writeTypedList(durls)
        dest?.writeString(file)
    }
}
