package com.andrehaueisen.cronicalia.b_firebase

import android.app.Activity
import android.content.Context
import android.util.Log
import com.andrehaueisen.cronicalia.*
import com.andrehaueisen.cronicalia.models.User
import com.andrehaueisen.cronicalia.utils.extensions.encodeEmail
import com.andrehaueisen.cronicalia.utils.extensions.pullStringFromSharedPreferences
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

/**
 * Created by andre on 2/18/2018.
 */
class Authenticator(
    private val mContext: Context,
    private val mDatabaseInstance: FirebaseFirestore,
    private val mFirebaseAuth: FirebaseAuth,
    private val mUser: User) {

    private val LOG_TAG = Authenticator::class.java.simpleName

    fun isUserLoggedIn() = mFirebaseAuth.currentUser != null

    fun getUserEncodedEmail() = mFirebaseAuth.currentUser?.email?.encodeEmail()
    fun getUserName() = mFirebaseAuth.currentUser?.displayName

    fun getCurrentUser(): FirebaseUser? {
        return mFirebaseAuth.currentUser
    }

    fun startLoginFlow(activity: Activity) = activity.startActivityForResult(
        AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(
                listOf(
                    AuthUI.IdpConfig.GoogleBuilder().build(),
                    AuthUI.IdpConfig.FacebookBuilder().build(),
                    AuthUI.IdpConfig.TwitterBuilder().build(),
                    AuthUI.IdpConfig.EmailBuilder().build()
                )
            )
            //.setLogo(R.drawable.ic_launcher)
            .setTheme(R.style.LogInTheme)
            .build(), LOGIN_REQUEST_CODE
    )

    fun saveUserIdOnLogin() {
        val currentUser = mFirebaseAuth.currentUser

        if (currentUser != null) {
            val encodedEmail = currentUser.email?.encodeEmail()
            val database = mDatabaseInstance.collection(COLLECTION_CREDENTIALS).document(DOCUMENT_UID_MAPPINGS)

            val emailUidMap = mapOf(Pair(encodedEmail!!, currentUser.uid))
            database.set(emailUidMap, SetOptions.merge())
            sendRegistrationToServer(encodedEmail)
        }
    }

    private fun sendRegistrationToServer(email: String?) {
        val token = mContext.pullStringFromSharedPreferences(SHARED_MESSAGE_TOKEN)

        if (email != null) {
            val emailTokenMap = mapOf(Pair(email, token))
            mDatabaseInstance.collection(COLLECTION_CREDENTIALS).document(DOCUMENT_MESSAGE_TOKENS).set(emailTokenMap, SetOptions.merge())
                .addOnSuccessListener { Log.i(LOG_TAG, "Message token sent to server") }
                .addOnFailureListener { Log.e(LOG_TAG, "Token not sent to server! Device won`t receive notifications") }

        }
    }

    fun logout() {
        mUser.refreshUser(User("Unknown", ""))
        mFirebaseAuth.signOut()
    }
}