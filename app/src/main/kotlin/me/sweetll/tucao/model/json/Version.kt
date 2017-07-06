package me.sweetll.tucao.model.json

import android.os.Parcel
import android.os.Parcelable

data class Version(val status: Int,
                   val versionCode: Int,
                   val versionName: String,
                   val description: String,
                   val apkUrl: String,
                   val apkSize: Long,
                   val patchUrl: String,
                   val patchSize: Long) : Parcelable {
    companion object {
        @JvmField val CREATOR: Parcelable.Creator<Version> = object : Parcelable.Creator<Version> {
            override fun createFromParcel(source: Parcel): Version = Version(source)
            override fun newArray(size: Int): Array<Version?> = arrayOfNulls(size)
        }
    }

    constructor(source: Parcel) : this(
    source.readInt(),
    source.readInt(),
    source.readString(),
    source.readString(),
    source.readString(),
    source.readLong(),
    source.readString(),
    source.readLong()
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(status)
        dest.writeInt(versionCode)
        dest.writeString(versionName)
        dest.writeString(description)
        dest.writeString(apkUrl)
        dest.writeLong(apkSize)
        dest.writeString(patchUrl)
        dest.writeLong(patchSize)
    }
}
