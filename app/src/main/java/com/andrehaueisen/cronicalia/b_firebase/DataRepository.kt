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
class DataRepository(private val mDatabaseInstance: FirebaseFirestore, 
                     private val mGlobalProgressBroadcastChannel: ArrayBroadcastChannel<Int?>,
                     private val mGlobalProgressReceiver: SubscriptionReceiveChannel<Int?>,
                     private val mUser: User) {

    /*fun createUser(user: User, uid: String){
        val batch = mDatabaseInstance.batch()

        val userDataLocationReference = mDatabaseInstance.collection(COLLECTION_USERS).document(user.encodedEmail!!)
        val uidDataLocationReference = mDatabaseInstance.collection(COLLECTION_USERS).document(DOCUMENT_EMAIL_UID)

        batch.set(userDataLocationReference, user)
        batch.set(uidDataLocationReference, Pair(user.encodedEmail, uid))
        batch.commit()
    }*/

    fun createBook(book: Book){
        val batch = mDatabaseInstance.batch()
        mUser.books[book.generateDocumentId()] = book

        val userDataLocationReference = mDatabaseInstance.collection(COLLECTION_USERS).document(mUser.encodedEmail!!)
        val booksLocationReference = mDatabaseInstance.collection(book.getDatabaseCollectionLocation()).document(book.generateDocumentId() )

        batch.set(userDataLocationReference, mUser, SetOptions.merge())
        batch.set(booksLocationReference, book, SetOptions.merge())

        batch
            .commit()
            .addOnSuccessListener{ _ ->
                launch(UI) { mGlobalProgressBroadcastChannel.send(UPLOAD_STATUS_OK) } }
            .addOnFailureListener( { _ ->
                launch(UI) { mGlobalProgressBroadcastChannel.send(UPLOAD_STATUS_FAIL) } })

    }

    fun loadLoggingInUser(userEncodedEmail: String, activity: Activity) {
        mDatabaseInstance.collection(COLLECTION_USERS).document(userEncodedEmail).get()
            .addOnSuccessListener { taskSnapshot ->
                val newUser = taskSnapshot.toObject(User::class.java)
                mUser.refreshUser(newUser)

                if(activity is LoginActivity){
                    activity.startCallingActivity()
                }
            }.addOnFailureListener { _ ->
                Toasty.error(activity, activity.getString(R.string.check_internet_connection)).show()
            }
    }

    fun createUserOnServer(userName: String, userEncodedEmail: String){
        mUser.name = userName
        mUser.encodedEmail = userEncodedEmail

        mDatabaseInstance.collection(COLLECTION_USERS).document(userEncodedEmail).set(mUser, SetOptions.merge())
            .addOnCompleteListener { Log.i("DataRepository", "New user sent to database") }
    }

    fun getUser() = mUser


}