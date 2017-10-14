package me.sweetll.tucao.business.drrr.model

import android.os.Parcel
import android.os.Parcelable

data class Post(
        val id: String,
        val content: String,
        var voteNum: Int,
        val replyNum: Int,
        val brand: String,
        val model: String,
        val systemVersion: String,
        val appVersion: String,
        val sticky: Boolean,
        val createDt: Long,
        val updateDt: Long,
        var vote: Boolean
) : Parcelable {
    constructor(source: Parcel) : this(
            source.readString(),
            source.readString(),
            source.readInt(),
            source.readInt(),
            source.readString(),
            source.readString(),
            source.readString(),
            source.readString(),
            1 == source.readInt(),
            source.readLong(),
            source.readLong(),
            1 == source.readInt()
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeString(id)
        writeString(content)
        writeInt(voteNum)
        writeInt(replyNum)
        writeString(brand)
        writeString(model)
        writeString(systemVersion)
        writeString(appVersion)
        writeInt((if (sticky) 1 else 0))
        writeLong(createDt)
        writeLong(updateDt)
        writeInt((if (vote) 1 else 0))
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<Post> = object : Parcelable.Creator<Post> {
            override fun createFromParcel(source: Parcel): Post = Post(source)
            override fun newArray(size: Int): Array<Post?> = arrayOfNulls(size)
        }
    }
}