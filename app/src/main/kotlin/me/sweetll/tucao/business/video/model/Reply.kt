package me.sweetll.tucao.business.video.model

import android.os.Parcel
import android.os.Parcelable
import com.squareup.moshi.Json
import retrofit2.http.Field

data class Reply(val id: String,
                 val avatar: String,
                 val nickname: String,
                 @field:Json(name = "creat_at") val time: String,
                 val content: String,
                 @field:Json(name = "userid") val userId: String,
                 @field:Json(name = "username")val userName: String,
                 var hasSend: Boolean = true) : Parcelable {
    constructor(source: Parcel) : this(
            source.readString(),
            source.readString(),
            source.readString(),
            source.readString(),
            source.readString(),
            source.readString(),
            source.readString(),
            1 == source.readInt()
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
        writeInt((if (hasSend) 1 else 0))
    }

    companion object {
        @JvmField val CREATOR: Parcelable.Creator<Reply> = object : Parcelable.Creator<Reply> {
            override fun createFromParcel(source: Parcel): Reply = Reply(source)
            override fun newArray(size: Int): Array<Reply?> = arrayOfNulls(size)
        }
    }
}

