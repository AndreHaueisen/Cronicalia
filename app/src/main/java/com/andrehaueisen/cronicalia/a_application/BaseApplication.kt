package com.andrehaueisen.cronicalia.a_application

import android.app.Activity
import android.app.Application
import android.util.Log
import com.andrehaueisen.cronicalia.COLLECTION_USERS
import com.andrehaueisen.cronicalia.a_application.dagger.ApplicationComponent
import com.andrehaueisen.cronicalia.a_application.dagger.ApplicationModule
import com.andrehaueisen.cronicalia.a_application.dagger.ContextModule
import com.andrehaueisen.cronicalia.a_application.dagger.DaggerApplicationComponent
import com.andrehaueisen.cronicalia.models.User
import com.andrehaueisen.cronicalia.utils.extensions.encodeEmail
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.experimental.channels.ArrayBroadcastChannel



/**
 * Created by andre on 2/18/2018.
 */
class BaseApplication : Application() {

    private lateinit var mComponent : ApplicationComponent

    companion object{
        fun get(activity: Activity): BaseApplication = activity.application as BaseApplication
    }

    enum class FileUrlId{
        POSTER, COVER, FULL_BOOK, CHAPTER
    }

    override fun onCreate() {
        super.onCreate()

        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()

        FirebaseApp.initializeApp(this)
        val storageReference = FirebaseStorage.getInstance().reference
        val databaseReference = FirebaseFirestore.getInstance()
        databaseReference.firestoreSettings = settings
        val currentLoggedUser = FirebaseAuth.getInstance().currentUser
        val user = User()

        currentLoggedUser?.let {
            user.name = currentLoggedUser.displayName
            user.encodedEmail = currentLoggedUser.email?.encodeEmail()
            listenToUser(databaseReference, user)
        }

        val globalProgressBroadcastChannel = ArrayBroadcastChannel<Int?>(6)
        val globalProgressReceiver = globalProgressBroadcastChannel.openSubscription()

        mComponent = DaggerApplicationComponent.builder()
            .applicationModule(ApplicationModule(
                storageReference,
                databaseReference,
                FirebaseAuth.getInstance(),
                globalProgressBroadcastChannel,
                globalProgressReceiver,
                user))
            .contextModule(ContextModule(this))
            .build()
    }

    private fun listenToUser(databaseReference: FirebaseFirestore, user: User): ListenerRegistration {

        val listener = EventListener<DocumentSnapshot>{ documentSnapshot, exception ->
            exception?.let {
                Log.e("LoginActivity", "User fetch failed")
            }

            if (documentSnapshot != null && documentSnapshot.exists()) {
                val newUser = documentSnapshot.toObject(User::class.java)
                user.refreshUser(newUser)
            }
        }

        return databaseReference.collection(COLLECTION_USERS).document(user.encodedEmail!!).addSnapshotListener(listener)
    }



    fun getAppComponent(): ApplicationComponent = mComponent
}