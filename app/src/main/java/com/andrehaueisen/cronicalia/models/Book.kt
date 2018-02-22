package com.andrehaueisen.cronicalia.models

import android.os.Parcel
import android.os.Parcelable

/**
 * Created by andre on 2/18/2018.
 */
data class Book(
    var title: String,
    var genre: BookGenre = BookGenre.UNDEFINED,
    var rating: Float = 0F,
    var vote: Int = 0,
    var income: Float = 0F,
    var readingNumber: Int = 0,
    var language: BookLanguage = BookLanguage.UNDEFINED,
    var chaptersTitles: ArrayList<String> = ArrayList(5),
    var chaptersUris: ArrayList<String> = ArrayList(5),
    var coverUri: String? = null,
    var isComplete: Boolean = false,
    var periodicity: ChapterPeriodicity = ChapterPeriodicity.NONE

) : Parcelable {
    enum class BookGenre {
        UNDEFINED,
        ACTION,
        FICTION,
        ROMANCE,
        COMEDY,
        DRAMA,
        HORROR,
        SATIRE,
        FANTASY,
        MYTHOLOGY,
        ADVENTURE
    }

    enum class BookLanguage {
        UNDEFINED,
        ENGLISH,
        PORTUGUESE,
        DEUTCH
    }

    enum class ChapterPeriodicity(private var periodicity: Int) {
        NONE(0),
        EVERY_DAY(1),
        EVERY_3_DAYS(3),
        EVERY_7_DAYS(7),
        EVERY_14_DAYS(14),
        EVERY_30_DAYS(30),
        EVERY_42_DAYS(42);

        public fun getPeriodicity() = this.periodicity
    }

    constructor(source: Parcel) : this(
        source.readString(),
        BookGenre.values()[source.readInt()],
        source.readFloat(),
        source.readInt(),
        source.readFloat(),
        source.readInt(),
        BookLanguage.values()[source.readInt()],
        source.createStringArrayList(),
        source.createStringArrayList(),
        source.readString(),
        1 == source.readInt(),
        ChapterPeriodicity.values()[source.readInt()]
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeString(title)
        writeInt(genre.ordinal)
        writeFloat(rating)
        writeInt(vote)
        writeFloat(income)
        writeInt(readingNumber)
        writeInt(language.ordinal)
        writeStringList(chaptersTitles)
        writeStringList(chaptersUris)
        writeString(coverUri)
        writeInt((if (isComplete) 1 else 0))
        writeInt(periodicity.ordinal)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<Book> = object : Parcelable.Creator<Book> {
            override fun createFromParcel(source: Parcel): Book = Book(source)
            override fun newArray(size: Int): Array<Book?> = arrayOfNulls(size)
        }
    }
}