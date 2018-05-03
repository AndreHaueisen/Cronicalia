package com.andrehaueisen.cronicalia.models

import android.content.Context
import android.content.res.Resources
import android.os.Parcel
import android.os.Parcelable
import android.support.v4.os.ConfigurationCompat
import com.andrehaueisen.cronicalia.*
import com.google.firebase.firestore.Exclude
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by andre on 2/18/2018.
 */
data class Book(
    var title: String? = null,
    var originalImmutableTitle: String? = null,
    var authorName: String? = null,
    var authorEmailId: String? = null,
    var authorTwitterAccount: String? = null,
    var publicationDate: Long = 0,
    var genre: BookGenre = BookGenre.UNDEFINED,
    var bookPosition: Int = 0,
    var rating: Float = 0F,
    var ratingCounter: Int = 0,
    var income: Float = 0F,
    var readingsNumber: Int = 0,
    var language: BookLanguage = BookLanguage.UNDEFINED,
    var localFullBookUri: String? = null,
    var remoteFullBookUri: String? = null,
    val remoteChapterTitles: ArrayList<String> = arrayListOf(),
    val remoteChapterUris: ArrayList<String> = arrayListOf(),
    val lastChapterLaunchDate: Long = 0,
    var localCoverUri: String? = null,
    var localPosterUri: String? = null,
    var remoteCoverUri: String? = null,
    var remotePosterUri: String? = null,
    var isLaunchedComplete: Boolean = true,
    var isCurrentlyComplete: Boolean = true,
    var periodicity: ChapterPeriodicity = ChapterPeriodicity.NONE,
    var synopsis: String? = null

) : Serializable, Parcelable {
    enum class BookGenre {
        UNDEFINED,
        ACTION,
        ADVENTURE,
        COMEDY,
        DRAMA,
        FANTASY,
        FICTION,
        HORROR,
        MYTHOLOGY,
        ROMANCE,
        SATIRE
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

    fun convertGenreToPosition(): Int {
        return when (genre) {
            Book.BookGenre.ACTION -> 0
            Book.BookGenre.ADVENTURE -> 1
            Book.BookGenre.COMEDY -> 2
            Book.BookGenre.DRAMA -> 3
            Book.BookGenre.FANTASY -> 4
            Book.BookGenre.FICTION -> 5
            Book.BookGenre.HORROR -> 6
            Book.BookGenre.MYTHOLOGY -> 7
            Book.BookGenre.ROMANCE -> 8
            Book.BookGenre.SATIRE -> 9
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

    fun convertRawDateToString(resources: Resources): String{

        val calendar = Calendar.getInstance()
        calendar.timeInMillis = publicationDate
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", ConfigurationCompat.getLocales(resources.configuration).get(0))

        return dateFormat.format(calendar.time)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val that = other as Book
        return title == that.title && // Possible to not use "id"?
                originalImmutableTitle == that.originalImmutableTitle &&
                authorName == that.authorName &&
                authorEmailId == that.authorEmailId &&
                authorTwitterAccount == that.authorTwitterAccount &&
                publicationDate == that.publicationDate &&
                genre == that.genre &&
                bookPosition == that.bookPosition &&
                rating == that.rating &&
                ratingCounter == that.ratingCounter &&
                income == that.income &&
                readingsNumber == that.readingsNumber &&
                language == that.language &&
                localFullBookUri == that.localFullBookUri &&
                remoteFullBookUri == that.remoteFullBookUri &&
                remoteChapterTitles == that.remoteChapterTitles &&
                remoteChapterUris == that.remoteChapterUris &&
                lastChapterLaunchDate == that.lastChapterLaunchDate &&
                localCoverUri == that.localCoverUri &&
                localPosterUri == that.localPosterUri &&
                remoteCoverUri == that.remoteCoverUri &&
                remotePosterUri == that.remotePosterUri &&
                isLaunchedComplete == that.isLaunchedComplete &&
                isCurrentlyComplete == that.isCurrentlyComplete

    }

    override fun hashCode(): Int {
        return Objects.hash(
            title,
            originalImmutableTitle,
            authorName,
            authorEmailId,
            authorTwitterAccount,
            publicationDate,
            genre,
            bookPosition,
            rating,
            ratingCounter,
            income,
            readingsNumber,
            language,
            localFullBookUri,
            remoteFullBookUri,
            remoteChapterTitles,
            remoteChapterUris,
            lastChapterLaunchDate,
            localCoverUri,
            localPosterUri,
            remoteCoverUri,
            remotePosterUri,
            isLaunchedComplete,
            isCurrentlyComplete,
            periodicity,
            synopsis
        )
    }

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

    override fun toString(): String {
        var result = "\n"
        remoteChapterUris.forEachIndexed { index, key ->
            result += "${remoteChapterTitles[index]} : $key\n"
        }

        return result
    }

    constructor(source: Parcel) : this(
        source.readString(),
        source.readString(),
        source.readString(),
        source.readString(),
        source.readString(),
        source.readLong(),
        BookGenre.values()[source.readInt()],
        source.readInt(),
        source.readFloat(),
        source.readInt(),
        source.readFloat(),
        source.readInt(),
        BookLanguage.values()[source.readInt()],
        source.readString(),
        source.readString(),
        source.createStringArrayList(),
        source.createStringArrayList(),
        source.readLong(),
        source.readString(),
        source.readString(),
        source.readString(),
        source.readString(),
        1 == source.readInt(),
        1 == source.readInt(),
        ChapterPeriodicity.values()[source.readInt()],
        source.readString()
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeString(title)
        writeString(originalImmutableTitle)
        writeString(authorName)
        writeString(authorEmailId)
        writeString(authorTwitterAccount)
        writeLong(publicationDate)
        writeInt(genre.ordinal)
        writeInt(bookPosition)
        writeFloat(rating)
        writeInt(ratingCounter)
        writeFloat(income)
        writeInt(readingsNumber)
        writeInt(language.ordinal)
        writeString(localFullBookUri)
        writeString(remoteFullBookUri)
        writeStringList(remoteChapterTitles)
        writeStringList(remoteChapterUris)
        writeLong(lastChapterLaunchDate)
        writeString(localCoverUri)
        writeString(localPosterUri)
        writeString(remoteCoverUri)
        writeString(remotePosterUri)
        writeInt((if (isLaunchedComplete) 1 else 0))
        writeInt((if (isCurrentlyComplete) 1 else 0))
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