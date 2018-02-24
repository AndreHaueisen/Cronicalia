package com.andrehaueisen.cronicalia.models

import android.os.Parcel
import android.os.Parcelable

/**
 * Created by andre on 2/18/2018.
 */
data class User(
    var name: String = "",
    var artisticName: String? = null,
    var aboutSelf: String? = null,
    var profilePictureUri: String? = null,
    var fans: Int = 0,
    var books: ArrayList<Book> = arrayListOf()) : Parcelable {

    fun getUserBookNumber() = books.size

    fun toSimpleMap(): Map<String, Any> {

        val simpleUserMap = HashMap<String, Any>()
        simpleUserMap["books"] = books

        return simpleUserMap
    }

    constructor(source: Parcel) : this(
        source.readString(),
        source.readString(),
        source.readString(),
        source.readString(),
        source.readInt(),
        source.createTypedArrayList(Book.CREATOR)
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeString(name)
        writeString(artisticName)
        writeString(aboutSelf)
        writeString(profilePictureUri)
        writeInt(fans)
        writeTypedList(books)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<User> = object : Parcelable.Creator<User> {
            override fun createFromParcel(source: Parcel): User = User(source)
            override fun newArray(size: Int): Array<User?> = arrayOfNulls(size)
        }
    }
}