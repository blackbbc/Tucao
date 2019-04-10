package me.sweetll.tucao.business.video.model

import android.os.Parcel
import android.os.Parcelable

data class Clicli(val code: Int,
                  val type: String,
                  val url: String) : Parcelable {
    constructor(source: Parcel) : this(
            source.readInt(),
            source.readString(),
            source.readString()
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeInt(code)
        writeString(type)
        writeString(url)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<Clicli> = object : Parcelable.Creator<Clicli> {
            override fun createFromParcel(source: Parcel): Clicli = Clicli(source)
            override fun newArray(size: Int): Array<Clicli?> = arrayOfNulls(size)
        }
    }
}