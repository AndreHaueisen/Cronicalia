package com.andrehaueisen.cronicalia.b_firebase

import com.andrehaueisen.cronicalia.COLLECTION_USERS
import com.andrehaueisen.cronicalia.DOCUMENT_EMAIL_UID
import com.andrehaueisen.cronicalia.UPLOAD_STATUS_FAIL
import com.andrehaueisen.cronicalia.UPLOAD_STATUS_OK
import com.andrehaueisen.cronicalia.models.Book
import com.andrehaueisen.cronicalia.models.User
import com.andrehaueisen.cronicalia.utils.extensions.encodeEmail
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.channels.ArrayBroadcastChannel
import kotlinx.coroutines.experimental.channels.SubscriptionReceiveChannel
import kotlinx.coroutines.experimental.launch

/**
 * Created by andre on 2/18/2018.
 */
class DataRepository(private val mDatabaseInstance: FirebaseFirestore, 
                     private val mGlobalProgressBroadcastChannel: ArrayBroadcastChannel<Double?>,
                     private val mGlobalProgressReceiver: SubscriptionReceiveChannel<Double?>, 
                     private val mUser: User) {

    enum class DataUploadStatus{
        UPLOAD_OK, UPLOAD_FAILED
    }

    fun createUser(user: User, uid: String){
        val batch = mDatabaseInstance.batch()

        val userDataLocationReference = mDatabaseInstance.collection(COLLECTION_USERS).document(user.encodedEmail)
        val uidDataLocationReference = mDatabaseInstance.collection(COLLECTION_USERS).document(DOCUMENT_EMAIL_UID)

        batch.set(userDataLocationReference, user)
        batch.set(uidDataLocationReference, Pair(user.encodedEmail, uid))
        batch.commit()
    }

    fun createBook(book: Book){
        val batch = mDatabaseInstance.batch()
        mUser.books[book.generateDocumentId()!!] = book

        val userDataLocationReference = mDatabaseInstance.collection(COLLECTION_USERS).document(mUser.encodedEmail)
        val booksLocationReference = mDatabaseInstance.collection(book.getDatabaseCollectionLocation()).document(book.generateDocumentId()!! )

        batch.set(userDataLocationReference, mUser, SetOptions.merge())
        batch.set(booksLocationReference, book, SetOptions.merge())

        batch
            .commit()
            .addOnSuccessListener{ _ ->
                launch(UI) { mGlobalProgressBroadcastChannel.send(UPLOAD_STATUS_OK) } }
            .addOnFailureListener( { _ ->
                launch(UI) { mGlobalProgressBroadcastChannel.send(UPLOAD_STATUS_FAIL) } })

    }

    fun updateUser(user: User, email: String){
        mDatabaseInstance.collection(COLLECTION_USERS).document(email.encodeEmail()).set(user, SetOptions.merge())
    }

    fun getUser() = mUser


}