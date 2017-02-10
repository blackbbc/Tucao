package me.sweetll.tucao.model.json

import android.os.Parcel
import android.os.Parcelable
import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class Result(val hid: String = "",
                  val title: String = "",
                  val play: Int = 0,
                  val mukio: Int = 0,
                  val create: String = "",
                  val thumb: String = "",
                  val typename: String = "",
                  val typeid: Int = 0,
                  val description: String = "",
                  val user: String = "",
                  val userid: String = "",
                  val keywords: String = "",
                  val part: Int = 0,
                  var video: MutableList<Video> = mutableListOf()) : Parcelable {
    companion object {
        @JvmField val CREATOR: Parcelable.Creator<Result> = object : Parcelable.Creator<Result> {
            override fun createFromParcel(source: Parcel): Result = Result(source)
            override fun newArray(size: Int): Array<Result?> = arrayOfNulls(size)
        }
    }

    constructor(source: Parcel) : this(source.readString(), source.readString(), source.readInt(), source.readInt(), source.readString(), source.readString(), source.readString(), source.readInt(), source.readString(), source.readString(), source.readString(), source.readString(), source.readInt(), source.createTypedArrayList(Video.CREATOR))

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeString(hid)
        dest?.writeString(title)
        dest?.writeInt(play)
        dest?.writeInt(mukio)
        dest?.writeString(create)
        dest?.writeString(thumb)
        dest?.writeString(typename)
        dest?.writeInt(typeid)
        dest?.writeString(description)
        dest?.writeString(user)
        dest?.writeString(userid)
        dest?.writeString(keywords)
        dest?.writeInt(part)
        dest?.writeTypedList(video)
    }
}
