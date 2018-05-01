package com.andrehaueisen.cronicalia.models

import android.os.Parcel
import android.os.Parcelable

data class BookOpinion(
    var userName: String,
    var opinion: String,
    var rating: Float
) : Parcelable {
    constructor(source: Parcel) : this(
        source.readString(),
        source.readString(),
        source.readFloat()
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeString(userName)
        writeString(opinion)
        writeFloat(rating)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<BookOpinion> = object : Parcelable.Creator<BookOpinion> {
            override fun createFromParcel(source: Parcel): BookOpinion = BookOpinion(source)
            override fun newArray(size: Int): Array<BookOpinion?> = arrayOfNulls(size)
        }
    }
}