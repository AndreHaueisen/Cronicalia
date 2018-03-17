package com.andrehaueisen.cronicalia.models

import android.os.Parcel
import android.os.Parcelable
import com.andrehaueisen.cronicalia.*
import com.google.firebase.firestore.Exclude

/**
 * Created by andre on 2/18/2018.
 */
data class Book(
    var title: String? = null,
    var authorName: String? = null,
    var authorEmailId: String? = null,
    var genre: BookGenre = BookGenre.UNDEFINED,
    var rating: Float = 0F,
    var vote: Int = 0,
    var income: Float = 0F,
    var readingNumber: Int = 0,
    var language: BookLanguage = BookLanguage.UNDEFINED,
    var localFullBookUri: String? = null,
    var remoteFullBookUri: String? = null,
    val localMapChapterUriTitle: HashMap<String, String> = hashMapOf(),
    val remoteMapChapterUriTitle: HashMap<String, String> = hashMapOf(),
    var localCoverUri: String? = null,
    var localPosterUri: String? = null,
    var remoteCoverUri: String? = null,
    var remotePosterUri: String? = null,
    var isComplete: Boolean = true,
    var periodicity: ChapterPeriodicity = ChapterPeriodicity.NONE,
    var synopsis: String? = null) : Parcelable {

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
        DEUTSCH
    }

    enum class ChapterPeriodicity(private var periodicity: Int) {
        NONE(0),
        EVERY_DAY(1),
        EVERY_3_DAYS(3),
        EVERY_7_DAYS(7),
        EVERY_14_DAYS(14),
        EVERY_30_DAYS(30),
        EVERY_42_DAYS(42);

        fun getPeriodicity() = this.periodicity
    }

    @Exclude
    fun getDatabaseCollectionLocation() = when (language) {
        BookLanguage.ENGLISH -> COLLECTION_BOOKS_ENGLISH
        BookLanguage.PORTUGUESE -> COLLECTION_BOOKS_PORTUGUESE
        BookLanguage.DEUTSCH -> COLLECTION_BOOKS_DEUTSCH
        else -> COLLECTION_BOOKS_ENGLISH
    }

    @Exclude
    fun getStorageRootLocation() = when (language) {
        BookLanguage.ENGLISH -> STORAGE_ENGLISH_BOOKS
        BookLanguage.PORTUGUESE -> STORAGE_PORTUGUESE_BOOKS
        BookLanguage.DEUTSCH -> STORAGE_DEUTSCH_BOOKS
        else -> STORAGE_ENGLISH_BOOKS
    }

    fun generateDocumentId(): String {
        return "${authorEmailId}_${title?.replace(" ", "")?.toLowerCase()}_$language"

    }

    fun generateChapterRepositoryTitle(chapterNumber: Int, value: String): String {
        return "chapter_${chapterNumber}_$value"
    }

    fun getChapterOriginalTitle(chapterNumber: Int, key: String): String {
        return localMapChapterUriTitle[key]!!.removePrefix("chapter_$chapterNumber")
    }

    constructor(source: Parcel) : this(
        source.readString(),
        source.readString(),
        source.readString(),
        BookGenre.values()[source.readInt()],
        source.readFloat(),
        source.readInt(),
        source.readFloat(),
        source.readInt(),
        BookLanguage.values()[source.readInt()],
        source.readString(),
        source.readString(),
        source.readSerializable() as HashMap<String, String>,
        source.readSerializable() as HashMap<String, String>,
        source.readString(),
        source.readString(),
        source.readString(),
        source.readString(),
        1 == source.readInt(),
        ChapterPeriodicity.values()[source.readInt()],
        source.readString()
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeString(title)
        writeString(authorName)
        writeString(authorEmailId)
        writeInt(genre.ordinal)
        writeFloat(rating)
        writeInt(vote)
        writeFloat(income)
        writeInt(readingNumber)
        writeInt(language.ordinal)
        writeString(localFullBookUri)
        writeString(remoteFullBookUri)
        writeSerializable(localMapChapterUriTitle)
        writeSerializable(remoteMapChapterUriTitle)
        writeString(localCoverUri)
        writeString(localPosterUri)
        writeString(remoteCoverUri)
        writeString(remotePosterUri)
        writeInt((if (isComplete) 1 else 0))
        writeInt(periodicity.ordinal)
        writeString(synopsis)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<Book> = object : Parcelable.Creator<Book> {
            override fun createFromParcel(source: Parcel): Book = Book(source)
            override fun newArray(size: Int): Array<Book?> = arrayOfNulls(size)
        }
    }
}