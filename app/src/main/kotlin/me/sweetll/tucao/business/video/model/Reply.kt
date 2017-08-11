package me.sweetll.tucao.business.video.model

import android.os.Parcel
import android.os.Parcelable

data class Reply(val id: String,
                 val avatar: String,
                 val nickname: String,
                 val time: String,
                 val content: String,
                 val userId: String,
                 val userName: String) : Parcelable {
    constructor(source: Parcel) : this(
            source.readString(),
            source.readString(),
            source.readString(),
            source.readString(),
            source.readString(),
            source.readString(),
            source.readString()
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeString(id)
        writeString(avatar)
        writeString(nickname)
        writeString(time)
        writeString(content)
        writeString(userId)
        writeString(userName)
    }

    companion object {
        @JvmField val CREATOR: Parcelable.Creator<Reply> = object : Parcelable.Creator<Reply> {
            override fun createFromParcel(source: Parcel): Reply = Reply(source)
            override fun newArray(size: Int): Array<Reply?> = arrayOfNulls(size)
        }
    }
}
