package me.sweetll.tucao.business.video.model

import android.os.Parcel
import android.os.Parcelable

data class Comment(val avatar: String,
                   val level: String,
                   val nickname: String,
                   var thumbUp: Int,
                   val lch: String,
                   val time: String,
                   val info: String,
                   val id: String,
                   val replyNum: Int,
                   var hasSend: Boolean = true,
                   var support: Boolean = false) : Parcelable {
    constructor(source: Parcel) : this(
            source.readString(),
            source.readString(),
            source.readString(),
            source.readInt(),
            source.readString(),
            source.readString(),
            source.readString(),
            source.readString(),
            source.readInt(),
            1 == source.readInt(),
            1 == source.readInt()
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeString(avatar)
        writeString(level)
        writeString(nickname)
        writeInt(thumbUp)
        writeString(lch)
        writeString(time)
        writeString(info)
        writeString(id)
        writeInt(replyNum)
        writeInt((if (hasSend) 1 else 0))
        writeInt((if (support) 1 else 0))
    }

    companion object {
        @JvmField val CREATOR: Parcelable.Creator<Comment> = object : Parcelable.Creator<Comment> {
            override fun createFromParcel(source: Parcel): Comment = Comment(source)
            override fun newArray(size: Int): Array<Comment?> = arrayOfNulls(size)
        }
    }
}
