package com.andrehaueisen.cronicalia.models

import android.os.Parcel
import android.os.Parcelable

/**
 * Created by andre on 2/18/2018.
 */
data class Book(val title: String, val chapters: ArrayList<String>) : Parcelable {

    constructor(source: Parcel) : this(
        source.readString(),
        source.createStringArrayList()
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeString(title)
        writeStringList(chapters)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<Book> = object : Parcelable.Creator<Book> {
            override fun createFromParcel(source: Parcel): Book = Book(source)
            override fun newArray(size: Int): Array<Book?> = arrayOfNulls(size)
        }
    }
}