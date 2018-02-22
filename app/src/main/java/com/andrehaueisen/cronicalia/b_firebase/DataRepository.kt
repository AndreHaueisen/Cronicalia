package com.andrehaueisen.cronicalia.b_firebase

import com.andrehaueisen.cronicalia.COLLECTION_USERS
import com.andrehaueisen.cronicalia.DOCUMENT_EMAIL_UID
import com.andrehaueisen.cronicalia.models.User
import com.andrehaueisen.cronicalia.utils.extensions.encodeEmail
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

/**
 * Created by andre on 2/18/2018.
 */
class DataRepository(val databaseInstance: FirebaseFirestore) {

    fun createUser(user: User, email: String, uid: String){
        val batch = databaseInstance.batch()

        val encodedEmail = email.encodeEmail()

        val userDataLocationReference = databaseInstance.collection(COLLECTION_USERS).document(encodedEmail)
        val uidDataLocationReference = databaseInstance.collection(COLLECTION_USERS).document(DOCUMENT_EMAIL_UID)

        batch.set(userDataLocationReference, user)
        batch.set(uidDataLocationReference, Pair(encodedEmail, uid))
        batch.commit()
    }

    fun updateUser(user: User, email: String){
        databaseInstance.collection(COLLECTION_USERS).document(email.encodeEmail()).set(user, SetOptions.merge())
    }


}