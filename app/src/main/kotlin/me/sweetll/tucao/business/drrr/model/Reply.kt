package me.sweetll.tucao.business.drrr.model

import android.os.Parcel
import android.os.Parcelable

data class Reply(val id: String,
                 val content: String,
                 val brand: String,
                 val model: String,
                 val systemVersion: String,
                 val appVersion: String,
                 val createDt: Long) : Parcelable {
    constructor(source: Parcel) : this(
            source.readString(),
            source.readString(),
            source.readString(),
            source.readString(),
            source.readString(),
            source.readString(),
            source.readLong()
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeString(id)
        writeString(content)
        writeString(brand)
        writeString(model)
        writeString(systemVersion)
        writeString(appVersion)
        writeLong(createDt)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<Reply> = object : Parcelable.Creator<Reply> {
            override fun createFromParcel(source: Parcel): Reply = Reply(source)
            override fun newArray(size: Int): Array<Reply?> = arrayOfNulls(size)
        }
    }
}
