package com.andrehaueisen.cronicalia.models

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import com.andrehaueisen.cronicalia.*
import com.google.firebase.firestore.Exclude
import java.io.Serializable
import java.util.*

/**
 * Created by andre on 2/18/2018.
 */
data class Book(
    var title: String? = null,
    var originalImmutableTitle: String? = null,
    var authorName: String? = null,
    var authorEmailId: String? = null,
    var genre: BookGenre = BookGenre.UNDEFINED,
    var bookPosition: Int = 0,
    var rating: Float = 0F,
    var voteCounter: Int = 0,
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
    var synopsis: String? = null) : Serializable, Parcelable {

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

    fun generateBookKey(): String {
        return "${authorEmailId}_${originalImmutableTitle?.replace(" ", "")?.toLowerCase()}_$language"
    }

    fun generateChapterRepositoryTitle(chapterNumber: Int, value: String): String {
        return "chapter_${chapterNumber}_$value"
    }

    fun convertGenreToPosition(): Int {
        return when (genre) {
            Book.BookGenre.ACTION -> 0
            Book.BookGenre.FICTION -> 1
            Book.BookGenre.ROMANCE -> 2
            Book.BookGenre.COMEDY -> 3
            Book.BookGenre.DRAMA -> 4
            Book.BookGenre.HORROR -> 5
            Book.BookGenre.SATIRE -> 6
            Book.BookGenre.FANTASY -> 7
            Book.BookGenre.MYTHOLOGY -> 8
            Book.BookGenre.ADVENTURE -> 9
            else -> 0
        }
    }

    fun convertPeriodicityToPosition(): Int {
        return when (periodicity) {
            ChapterPeriodicity.EVERY_DAY -> 0
            ChapterPeriodicity.EVERY_3_DAYS -> 1
            ChapterPeriodicity.EVERY_7_DAYS -> 2
            ChapterPeriodicity.EVERY_14_DAYS -> 3
            ChapterPeriodicity.EVERY_30_DAYS -> 4
            ChapterPeriodicity.EVERY_42_DAYS -> 5
            else -> 0
        }
    }

    fun generateChapterOriginalTitle(chapterNumber: Int, key: String): String {
        return localMapChapterUriTitle[key]!!.removePrefix("chapter_$chapterNumber")
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val that = other as Book
        return title == that.title && // Possible to not use "id"?
                originalImmutableTitle == that.originalImmutableTitle &&
                authorName == that.authorName &&
                authorEmailId == that.authorEmailId &&
                genre == that.genre &&
                rating == that.rating &&
                voteCounter == that.voteCounter &&
                income == that.income &&
                readingNumber == that.readingNumber &&
                language == that.language &&
                localFullBookUri == that.localFullBookUri &&
                remoteFullBookUri == that.remoteFullBookUri &&
                localMapChapterUriTitle == that.localMapChapterUriTitle &&
                remoteMapChapterUriTitle == that.remoteMapChapterUriTitle &&
                localCoverUri == that.localCoverUri &&
                localPosterUri == that.localPosterUri &&
                remoteCoverUri == that.remoteCoverUri &&
                remotePosterUri == that.remotePosterUri &&
                isComplete == that.isComplete

    }

    override fun hashCode(): Int {
        return Objects.hash(
            title,
            originalImmutableTitle,
            authorName,
            authorEmailId,
            genre,
            rating,
            voteCounter,
            income,
            readingNumber,
            language,
            localFullBookUri,
            remoteFullBookUri,
            localMapChapterUriTitle,
            remoteMapChapterUriTitle,
            localCoverUri,
            localPosterUri,
            remoteCoverUri,
            remotePosterUri,
            isComplete,
            periodicity,
            synopsis
        )
    }

    constructor(source: Parcel) : this(
        source.readString(),
        source.readString(),
        source.readString(),
        source.readString(),
        BookGenre.values()[source.readInt()],
        source.readInt(),
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

    @Exclude
    fun isBookTitleValid(context: Context): Boolean {
        return !this.title.isNullOrBlank() && this.title!!.replace(
            " ",
            ""
        ).length <= context.resources.getInteger(R.integer.title_text_box_max_length)
    }

    @Exclude
    fun isSynopsisValid(context: Context): Boolean {
        return !this.synopsis.isNullOrBlank() && this.synopsis!!.replace(
            " ",
            ""
        ).length <= context.resources.getInteger(R.integer.synopsis_text_box_max_length)
    }

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeString(title)
        writeString(originalImmutableTitle)
        writeString(authorName)
        writeString(authorEmailId)
        writeInt(genre.ordinal)
        writeInt(bookPosition)
        writeFloat(rating)
        writeInt(voteCounter)
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