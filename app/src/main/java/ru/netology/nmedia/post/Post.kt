package ru.netology.nmedia.post

import android.os.Parcel
import android.os.Parcelable

data class Post(
        val id: Long,
        val author: String,
        val published: String,
        val content: String?,
        val share: Int,
        val likes: Int,
        val views: Int,
        val url: String?,
        val likedByMe: Boolean = false
        ) : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readLong(),
            parcel.readString().toString(),
            parcel.readString().toString(),
            parcel.readString(),
            parcel.readInt(),
            parcel.readInt(),
            parcel.readInt(),
            parcel.readString(),
            parcel.readByte() != 0.toByte()) {
    }

    override fun describeContents(): Int {
        TODO("Not yet implemented")
    }


    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeString(author)
        parcel.writeString(published)
        parcel.writeString(content)
        parcel.writeInt(share)
        parcel.writeInt(likes)
        parcel.writeInt(views)
        parcel.writeString(url)
        parcel.writeByte(if (likedByMe) 1 else 0)
    }

    companion object CREATOR : Parcelable.Creator<Post> {
        override fun createFromParcel(parcel: Parcel): Post {
            return Post(parcel)
        }

        override fun newArray(size: Int): Array<Post?> {
            return arrayOfNulls(size)
        }
    }
}