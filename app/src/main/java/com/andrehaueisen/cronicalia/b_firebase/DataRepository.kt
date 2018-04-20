package com.andrehaueisen.cronicalia.b_firebase

import android.app.Activity
import android.util.Log
import com.andrehaueisen.cronicalia.COLLECTION_USERS
import com.andrehaueisen.cronicalia.R
import com.andrehaueisen.cronicalia.UPLOAD_STATUS_FAIL
import com.andrehaueisen.cronicalia.UPLOAD_STATUS_OK
import com.andrehaueisen.cronicalia.i_login.LoginActivity
import com.andrehaueisen.cronicalia.models.Book
import com.andrehaueisen.cronicalia.models.User
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import es.dmoral.toasty.Toasty
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.channels.ArrayBroadcastChannel
import kotlinx.coroutines.experimental.channels.SubscriptionReceiveChannel
import kotlinx.coroutines.experimental.launch


/**
 * Created by andre on 2/18/2018.
 */
class DataRepository(
    private val mDatabaseInstance: FirebaseFirestore,
    private val mGlobalProgressBroadcastChannel: ArrayBroadcastChannel<Int?>,
    private val mGlobalProgressReceiver: SubscriptionReceiveChannel<Int?>,
    private val mUser: User
) {

    /*fun createUser(user: User, uid: String){
        val batch = mDatabaseInstance.batch()

        val userDataLocationReference = mDatabaseInstance.collection(COLLECTION_USERS).document(user.encodedEmail!!)
        val uidDataLocationReference = mDatabaseInstance.collection(COLLECTION_USERS).document(DOCUMENT_EMAIL_UID)

        batch.set(userDataLocationReference, user)
        batch.set(uidDataLocationReference, Pair(user.encodedEmail, uid))
        batch.commit()
    }*/

    /**
     * Updates full book on user collection and on book collection. Creates if book does not exists
     */
    fun setBookDocuments(book: Book, sendProgressUpdate: Boolean = true) {
        val batch = mDatabaseInstance.batch()
        mUser.books[book.generateBookKey()] = book

        val userDataLocationReference = mDatabaseInstance.collection(COLLECTION_USERS).document(mUser.encodedEmail!!)
        val booksLocationReference = mDatabaseInstance.collection(book.getDatabaseCollectionLocation()).document(book.generateBookKey())

        batch.set(userDataLocationReference, mUser, SetOptions.merge())
        batch.set(booksLocationReference, book, SetOptions.merge())

        batch
            .commit()
            .addOnSuccessListener { _ ->
                if (sendProgressUpdate)
                    launch(UI) { mGlobalProgressBroadcastChannel.send(UPLOAD_STATUS_OK) }
            }
            .addOnFailureListener({ _ ->
                if (sendProgressUpdate)
                    launch(UI) { mGlobalProgressBroadcastChannel.send(UPLOAD_STATUS_FAIL) }
            })

    }

    fun updateBookPdfReferences(book: Book, sendProgressUpdate: Boolean = true) {

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

        batch
            .commit()
            .addOnSuccessListener { _ ->
            if (sendProgressUpdate)
                launch(UI) { mGlobalProgressBroadcastChannel.send(UPLOAD_STATUS_OK) }
            }
            .addOnFailureListener({ _ ->
                if (sendProgressUpdate)
                    launch(UI) { mGlobalProgressBroadcastChannel.send(UPLOAD_STATUS_FAIL) }
            })
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

    fun deleteBook(book: Book){
        val batch = mDatabaseInstance.batch()

        val userDataLocationReference = mDatabaseInstance.collection(COLLECTION_USERS).document(mUser.encodedEmail!!)
        val booksLocationReference = mDatabaseInstance.collection(book.getDatabaseCollectionLocation()).document(book.generateBookKey())

        val valuesToDeleteOnUserMap = HashMap<String, Any>()

        valuesToDeleteOnUserMap["books.${book.generateBookKey()}"] = FieldValue.delete()

        batch.update(userDataLocationReference, valuesToDeleteOnUserMap)
        batch.delete(booksLocationReference)
        batch.commit()
    }

    fun updateUserProfilePictureReferences(localProfileImageUri: String, remoteProfileImageUri: String){
        val userDataLocationReference = mDatabaseInstance.collection(COLLECTION_USERS).document(mUser.encodedEmail!!)
        val valuesToUpdate = mutableMapOf<String, Any>()
        valuesToUpdate["localProfilePictureUri"] = localProfileImageUri
        valuesToUpdate["remoteProfilePictureUri"] = remoteProfileImageUri

        userDataLocationReference.update(valuesToUpdate)
    }

    fun updateUserBackgroundPictureReferences(localBackgroundImageUri: String, remoteBackgroundImageUri: String){
        val userDataLocationReference = mDatabaseInstance.collection(COLLECTION_USERS).document(mUser.encodedEmail!!)
        val valuesToUpdate = mutableMapOf<String, Any>()
        valuesToUpdate["localBackgroundPictureUri"] = localBackgroundImageUri
        valuesToUpdate["remoteBackgroundPictureUri"] = remoteBackgroundImageUri

        userDataLocationReference.update(valuesToUpdate)
    }

    fun updateUserName(newName: String){
        val userDataLocationReference = mDatabaseInstance.collection(COLLECTION_USERS).document(mUser.encodedEmail!!)
        userDataLocationReference.update("name", newName)
    }

    fun updateUserArtisticName(newArtisticName: String){
        val userDataLocationReference = mDatabaseInstance.collection(COLLECTION_USERS).document(mUser.encodedEmail!!)
        userDataLocationReference.update("artisticName", newArtisticName)
    }

    fun updateUserAboutMe(newAboutMe: String){
        val userDataLocationReference = mDatabaseInstance.collection(COLLECTION_USERS).document(mUser.encodedEmail!!)
        userDataLocationReference.update("aboutMe", newAboutMe)

    }

    fun loadLoggingInUser(userName: String, userEncodedEmail: String, activity: Activity) {
        mDatabaseInstance.collection(COLLECTION_USERS).document(userEncodedEmail).get()
            .addOnSuccessListener { taskSnapshot ->
                val newUser = taskSnapshot.toObject(User::class.java)
                if(newUser == null){
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