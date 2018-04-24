package com.andrehaueisen.cronicalia.b_firebase

import android.app.Activity
import android.util.Log
import com.andrehaueisen.cronicalia.*
import com.andrehaueisen.cronicalia.i_login.LoginActivity
import com.andrehaueisen.cronicalia.models.Book
import com.andrehaueisen.cronicalia.models.User
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import es.dmoral.toasty.Toasty
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.channels.ArrayBroadcastChannel
import kotlinx.coroutines.experimental.launch
import kotlin.coroutines.experimental.Continuation
import kotlin.coroutines.experimental.suspendCoroutine


/**
 * Created by andre on 2/18/2018.
 */
class DataRepository(
    private val mDatabaseInstance: FirebaseFirestore,
    private val mUser: User
) {

    //Manage books

    /**
     * Updates full book on user collection and on book collection. Creates if book does not exists
     */
    fun setBookDocuments(book: Book, continuation: Continuation<Int>? = null, progressBroadcastChannel: ArrayBroadcastChannel<Int>? = null) {
        val batch = mDatabaseInstance.batch()
        mUser.books[book.generateBookKey()] = book

        val userDataLocationReference = mDatabaseInstance.collection(COLLECTION_USERS).document(mUser.encodedEmail!!)
        val booksLocationReference = mDatabaseInstance.collection(book.getDatabaseCollectionLocation()).document(book.generateBookKey())

        batch.set(userDataLocationReference, mUser, SetOptions.merge())
        batch.set(booksLocationReference, book, SetOptions.merge())

        batch
            .commit()
            .addOnSuccessListener { _ ->
                continuation?.resume(UPLOAD_STATUS_OK)
                progressBroadcastChannel?.let { launch(UI) { it.send(UPLOAD_STATUS_OK) } }
            }
            .addOnFailureListener({ _ ->
                continuation?.resume(UPLOAD_STATUS_FAIL)
                progressBroadcastChannel?.let { launch(UI) { it.send(UPLOAD_STATUS_FAIL) } }
            })

    }

    suspend fun updateBookPdfReferences(book: Book): Int {

        val batch = mDatabaseInstance.batch()

        val userDataLocationReference = mDatabaseInstance.collection(COLLECTION_USERS).document(mUser.encodedEmail!!)
        val booksLocationReference = mDatabaseInstance.collection(book.getDatabaseCollectionLocation()).document(book.generateBookKey())

        val valuesToUpdateOnUserMap = HashMap<String, Any>()
        val valuesToUpdateOnBooksLocationMap = HashMap<String, Any>()

        if (book.isLaunchedComplete) {
            valuesToUpdateOnUserMap["books.${book.generateBookKey()}.localFullBookUri"] = book.localFullBookUri!!
            valuesToUpdateOnUserMap["books.${book.generateBookKey()}.remoteFullBookUri"] = book.remoteFullBookUri!!
            valuesToUpdateOnBooksLocationMap["localFullBookUri"] = book.localFullBookUri!!
            valuesToUpdateOnBooksLocationMap["remoteFullBookUri"] = book.remoteFullBookUri!!

        } else {
            valuesToUpdateOnUserMap["books.${book.generateBookKey()}.remoteChapterTitles"] = book.remoteChapterTitles
            valuesToUpdateOnUserMap["books.${book.generateBookKey()}.remoteChapterUris"] = book.remoteChapterUris
            valuesToUpdateOnBooksLocationMap["remoteChapterTitles"] = book.remoteChapterTitles
            valuesToUpdateOnBooksLocationMap["remoteChapterUris"] = book.remoteChapterUris
        }

        batch.update(userDataLocationReference, valuesToUpdateOnUserMap)
        batch.update(booksLocationReference, valuesToUpdateOnBooksLocationMap)

        return async(CommonPool) {
            suspendCoroutine<Int> { continuation ->

                batch
                    .commit()
                    .addOnSuccessListener { _ ->
                        continuation.resume(UPLOAD_STATUS_OK)
                    }
                    .addOnFailureListener({ _ ->
                        continuation.resume(UPLOAD_STATUS_FAIL)
                    })
            }
        }.await()
    }

    fun updateBookTitle(newTitle: String, collectionLocation: String, bookKey: String) {

        val batch = mDatabaseInstance.batch()

        val userDataLocationReference = mDatabaseInstance.collection(COLLECTION_USERS).document(mUser.encodedEmail!!)
        val booksLocationReference = mDatabaseInstance.collection(collectionLocation).document(bookKey)

        batch.update(userDataLocationReference, "books.$bookKey.title", newTitle)
        batch.update(booksLocationReference, "title", newTitle)
        batch.commit()
    }

    fun updateBookSynopsis(newSynopsis: String, collectionLocation: String, bookKey: String) {
        val batch = mDatabaseInstance.batch()

        val userDataLocationReference = mDatabaseInstance.collection(COLLECTION_USERS).document(mUser.encodedEmail!!)
        val booksLocationReference = mDatabaseInstance.collection(collectionLocation).document(bookKey)

        batch.update(userDataLocationReference, "books.$bookKey.synopsis", newSynopsis)
        batch.update(booksLocationReference, "synopsis", newSynopsis)
        batch.commit()
    }

    fun updateBookGenre(newGenre: String, collectionLocation: String, bookKey: String) {
        val batch = mDatabaseInstance.batch()

        val userDataLocationReference = mDatabaseInstance.collection(COLLECTION_USERS).document(mUser.encodedEmail!!)
        val booksLocationReference = mDatabaseInstance.collection(collectionLocation).document(bookKey)

        batch.update(userDataLocationReference, "books.$bookKey.genre", newGenre)
        batch.update(booksLocationReference, "genre", newGenre)
        batch.commit()
    }

    fun updateBookPeriodicity(newPeriodicity: String, collectionLocation: String, bookKey: String) {
        val batch = mDatabaseInstance.batch()

        val userDataLocationReference = mDatabaseInstance.collection(COLLECTION_USERS).document(mUser.encodedEmail!!)
        val booksLocationReference = mDatabaseInstance.collection(collectionLocation).document(bookKey)

        batch.update(userDataLocationReference, "books.$bookKey.periodicity", newPeriodicity)
        batch.update(booksLocationReference, "periodicity", newPeriodicity)
        batch.commit()
    }

    suspend fun queryFeaturedActionBooks(bookLanguage: Book.BookLanguage): MutableList<Book> {

        val featuredActionsBooks = mutableListOf<Book>()

        return async(CommonPool) {
            suspendCoroutine<MutableList<Book>> { continuation ->
                mDatabaseInstance.collection(getBookCollectionRepository(bookLanguage))
                    .whereEqualTo("genre", Book.BookGenre.ACTION.toString())
                    .whereGreaterThan("rating", 8.0)
                    .get()
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val documentSnapshot = task.result
                            if (!documentSnapshot.isEmpty) {
                                documentSnapshot.forEach { document ->
                                    val book = document.toObject(Book::class.java)
                                    featuredActionsBooks.add(book)
                                }
                            }
                            continuation.resume(featuredActionsBooks)

                        } else {
                            task.exception?.let {
                                continuation.resumeWithException(task.exception!!)
                            }
                        }
                    }
            }
        }.await()

    }

    suspend fun queryFeaturedFictionBooks(bookLanguage: Book.BookLanguage): MutableList<Book> {

        val featuredFictionBooks = mutableListOf<Book>()

        return async(CommonPool) {
            suspendCoroutine<MutableList<Book>> { continuation ->
                mDatabaseInstance.collection(getBookCollectionRepository(bookLanguage))
                    .whereEqualTo("genre", Book.BookGenre.FICTION.toString())
                    .whereGreaterThan("rating", 8.0)
                    .get()
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val documentSnapshot = task.result
                            if (!documentSnapshot.isEmpty) {
                                documentSnapshot.forEach { document ->
                                    val book = document.toObject(Book::class.java)
                                    featuredFictionBooks.add(book)
                                }
                            }
                            continuation.resume(featuredFictionBooks)

                        } else {
                            task.exception?.let {
                                continuation.resumeWithException(task.exception!!)
                            }
                        }
                    }
            }
        }.await()

    }

    suspend fun queryFeaturedRomanceBooks(bookLanguage: Book.BookLanguage): MutableList<Book> {

        val featuredRomanceBooks = mutableListOf<Book>()

        return async(CommonPool) {
            suspendCoroutine<MutableList<Book>> { continuation ->
                mDatabaseInstance.collection(getBookCollectionRepository(bookLanguage))
                    .whereEqualTo("genre", Book.BookGenre.ROMANCE.toString())
                    .whereGreaterThan("rating", 8.0)
                    .get()
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val documentSnapshot = task.result
                            if (!documentSnapshot.isEmpty) {
                                documentSnapshot.forEach { document ->
                                    val book = document.toObject(Book::class.java)
                                    featuredRomanceBooks.add(book)
                                }
                            }
                            continuation.resume(featuredRomanceBooks)

                        } else {
                            task.exception?.let {
                                continuation.resumeWithException(task.exception!!)
                            }
                        }
                    }
            }
        }.await()

    }

    suspend fun queryFeaturedComedyBooks(bookLanguage: Book.BookLanguage): MutableList<Book> {

        val featuredComedyBooks = mutableListOf<Book>()

        return async(CommonPool) {
            suspendCoroutine<MutableList<Book>> { continuation ->
                mDatabaseInstance.collection(getBookCollectionRepository(bookLanguage))
                    .whereEqualTo("genre", Book.BookGenre.COMEDY.toString())
                    .whereGreaterThan("rating", 8.0)
                    .get()
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val documentSnapshot = task.result
                            if (!documentSnapshot.isEmpty) {
                                documentSnapshot.forEach { document ->
                                    val book = document.toObject(Book::class.java)
                                    featuredComedyBooks.add(book)
                                }
                            }
                            continuation.resume(featuredComedyBooks)

                        } else {
                            task.exception?.let {
                                continuation.resumeWithException(task.exception!!)
                            }
                        }
                    }
            }
        }.await()

    }

    suspend fun queryFeaturedDramaBooks(bookLanguage: Book.BookLanguage): MutableList<Book> {

        val featuredDramaBooks = mutableListOf<Book>()

        return async(CommonPool) {
            suspendCoroutine<MutableList<Book>> { continuation ->
                mDatabaseInstance.collection(getBookCollectionRepository(bookLanguage))
                    .whereEqualTo("genre", Book.BookGenre.DRAMA.toString())
                    .whereGreaterThan("rating", 8.0)
                    .get()
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val documentSnapshot = task.result
                            if (!documentSnapshot.isEmpty) {
                                documentSnapshot.forEach { document ->
                                    val book = document.toObject(Book::class.java)
                                    featuredDramaBooks.add(book)
                                }
                            }
                            continuation.resume(featuredDramaBooks)

                        } else {
                            task.exception?.let {
                                continuation.resumeWithException(task.exception!!)
                            }
                        }
                    }
            }
        }.await()

    }

    suspend fun queryFeaturedHorrorBooks(bookLanguage: Book.BookLanguage): MutableList<Book> {

        val featuredHorrorBooks = mutableListOf<Book>()

        return async(CommonPool) {
            suspendCoroutine<MutableList<Book>> { continuation ->
                mDatabaseInstance.collection(getBookCollectionRepository(bookLanguage))
                    .whereEqualTo("genre", Book.BookGenre.HORROR.toString())
                    .whereGreaterThan("rating", 8.0)
                    .get()
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val documentSnapshot = task.result
                            if (!documentSnapshot.isEmpty) {
                                documentSnapshot.forEach { document ->
                                    val book = document.toObject(Book::class.java)
                                    featuredHorrorBooks.add(book)
                                }
                            }
                            continuation.resume(featuredHorrorBooks)

                        } else {
                            task.exception?.let {
                                continuation.resumeWithException(task.exception!!)
                            }
                        }
                    }
            }
        }.await()

    }

    suspend fun queryFeaturedSatireBooks(bookLanguage: Book.BookLanguage): MutableList<Book> {

        val featuredSatireBooks = mutableListOf<Book>()

        return async(CommonPool) {
            suspendCoroutine<MutableList<Book>> { continuation ->
                mDatabaseInstance.collection(getBookCollectionRepository(bookLanguage))
                    .whereEqualTo("genre", Book.BookGenre.SATIRE.toString())
                    .whereGreaterThan("rating", 8.0)
                    .get()
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val documentSnapshot = task.result
                            if (!documentSnapshot.isEmpty) {
                                documentSnapshot.forEach { document ->
                                    val book = document.toObject(Book::class.java)
                                    featuredSatireBooks.add(book)
                                }
                            }
                            continuation.resume(featuredSatireBooks)

                        } else {
                            task.exception?.let {
                                continuation.resumeWithException(task.exception!!)
                            }
                        }
                    }
            }
        }.await()

    }

    suspend fun queryFeaturedFantasyBooks(bookLanguage: Book.BookLanguage): MutableList<Book> {

        val featuredFantasyBooks = mutableListOf<Book>()

        return async(CommonPool) {
            suspendCoroutine<MutableList<Book>> { continuation ->
                mDatabaseInstance.collection(getBookCollectionRepository(bookLanguage))
                    .whereEqualTo("genre", Book.BookGenre.FANTASY.toString())
                    .whereGreaterThan("rating", 8.0)
                    .get()
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val documentSnapshot = task.result
                            if (!documentSnapshot.isEmpty) {
                                documentSnapshot.forEach { document ->
                                    val book = document.toObject(Book::class.java)
                                    featuredFantasyBooks.add(book)
                                }
                            }
                            continuation.resume(featuredFantasyBooks)

                        } else {
                            task.exception?.let {
                                continuation.resumeWithException(task.exception!!)
                            }
                        }
                    }
            }
        }.await()

    }

    suspend fun queryFeaturedMythologyBooks(bookLanguage: Book.BookLanguage): MutableList<Book> {

        val featuredMythologyBooks = mutableListOf<Book>()

        return async(CommonPool) {
            suspendCoroutine<MutableList<Book>> { continuation ->
                mDatabaseInstance.collection(getBookCollectionRepository(bookLanguage))
                    .whereEqualTo("genre", Book.BookGenre.MYTHOLOGY.toString())
                    .whereGreaterThan("rating", 8.0)
                    .get()
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val documentSnapshot = task.result
                            if (!documentSnapshot.isEmpty) {
                                documentSnapshot.forEach { document ->
                                    val book = document.toObject(Book::class.java)
                                    featuredMythologyBooks.add(book)
                                }
                            }
                            continuation.resume(featuredMythologyBooks)

                        } else {
                            task.exception?.let {
                                continuation.resumeWithException(task.exception!!)
                            }
                        }
                    }
            }
        }.await()

    }

    suspend fun queryFeaturedAdventureBooks(bookLanguage: Book.BookLanguage): MutableList<Book> {

        val featuredAdventureBooks = mutableListOf<Book>()

        return async(CommonPool) {
            suspendCoroutine<MutableList<Book>> { continuation ->
                mDatabaseInstance.collection(getBookCollectionRepository(bookLanguage))
                    .whereEqualTo("genre", Book.BookGenre.ADVENTURE.toString())
                    .whereGreaterThan("rating", 8.0)
                    .get()
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val documentSnapshot = task.result
                            if (!documentSnapshot.isEmpty) {
                                documentSnapshot.forEach { document ->
                                    val book = document.toObject(Book::class.java)
                                    featuredAdventureBooks.add(book)
                                }
                            }
                            continuation.resume(featuredAdventureBooks)

                        } else {
                            task.exception?.let {
                                continuation.resumeWithException(task.exception!!)
                            }
                        }
                    }
            }
        }.await()

    }

    private fun getBookCollectionRepository(bookLanguage: Book.BookLanguage): String {

        return when (bookLanguage) {
            Book.BookLanguage.ENGLISH -> COLLECTION_BOOKS_ENGLISH
            Book.BookLanguage.PORTUGUESE -> COLLECTION_BOOKS_PORTUGUESE
            Book.BookLanguage.DEUTSCH -> COLLECTION_BOOKS_DEUTSCH
            Book.BookLanguage.UNDEFINED -> COLLECTION_BOOKS_ENGLISH
        }
    }

    fun deleteBook(book: Book) {
        val batch = mDatabaseInstance.batch()

        val userDataLocationReference = mDatabaseInstance.collection(COLLECTION_USERS).document(mUser.encodedEmail!!)
        val booksLocationReference = mDatabaseInstance.collection(book.getDatabaseCollectionLocation()).document(book.generateBookKey())

        val valuesToDeleteOnUserMap = HashMap<String, Any>()

        valuesToDeleteOnUserMap["books.${book.generateBookKey()}"] = FieldValue.delete()

        batch.update(userDataLocationReference, valuesToDeleteOnUserMap)
        batch.delete(booksLocationReference)
        batch.commit()
    }

    //Manage Users

    fun updateUserProfilePictureReferences(localProfileImageUri: String, remoteProfileImageUri: String) {
        val userDataLocationReference = mDatabaseInstance.collection(COLLECTION_USERS).document(mUser.encodedEmail!!)
        val valuesToUpdate = mutableMapOf<String, Any>()
        valuesToUpdate["localProfilePictureUri"] = localProfileImageUri
        valuesToUpdate["remoteProfilePictureUri"] = remoteProfileImageUri

        userDataLocationReference.update(valuesToUpdate)
    }

    fun updateUserBackgroundPictureReferences(localBackgroundImageUri: String, remoteBackgroundImageUri: String) {
        val userDataLocationReference = mDatabaseInstance.collection(COLLECTION_USERS).document(mUser.encodedEmail!!)
        val valuesToUpdate = mutableMapOf<String, Any>()
        valuesToUpdate["localBackgroundPictureUri"] = localBackgroundImageUri
        valuesToUpdate["remoteBackgroundPictureUri"] = remoteBackgroundImageUri

        userDataLocationReference.update(valuesToUpdate)
    }

    fun updateUserName(newName: String) {
        val userDataLocationReference = mDatabaseInstance.collection(COLLECTION_USERS).document(mUser.encodedEmail!!)
        userDataLocationReference.update("name", newName)
    }

    fun updateUserArtisticName(newArtisticName: String) {
        val userDataLocationReference = mDatabaseInstance.collection(COLLECTION_USERS).document(mUser.encodedEmail!!)
        userDataLocationReference.update("artisticName", newArtisticName)
    }

    fun updateUserAboutMe(newAboutMe: String) {
        val userDataLocationReference = mDatabaseInstance.collection(COLLECTION_USERS).document(mUser.encodedEmail!!)
        userDataLocationReference.update("aboutMe", newAboutMe)

    }

    fun loadLoggingInUser(userName: String, userEncodedEmail: String, activity: Activity) {
        mDatabaseInstance.collection(COLLECTION_USERS).document(userEncodedEmail).get()
            .addOnSuccessListener { taskSnapshot ->
                val newUser = taskSnapshot.toObject(User::class.java)
                if (newUser == null) {
                    setInitialUserDocument(userName, userEncodedEmail)
                } else {
                    mUser.refreshUser(newUser)
                }

                if (activity is LoginActivity) {
                    activity.startCallingActivity()
                }
            }.addOnFailureListener { _ ->
                Toasty.error(activity, activity.getString(R.string.check_internet_connection)).show()
            }
    }

    fun setInitialUserDocument(userName: String, userEncodedEmail: String) {
        mUser.name = userName
        mUser.encodedEmail = userEncodedEmail

        mDatabaseInstance.collection(COLLECTION_USERS).document(userEncodedEmail).set(mUser, SetOptions.merge())
            .addOnCompleteListener { Log.i("DataRepository", "New user sent to database") }
    }

    fun getUser() = mUser


}