package com.andrehaueisen.cronicalia.a_application

import android.app.Activity
import android.app.Application
import android.net.Uri
import com.andrehaueisen.cronicalia.a_application.dagger.ApplicationComponent
import com.andrehaueisen.cronicalia.a_application.dagger.ApplicationModule
import com.andrehaueisen.cronicalia.a_application.dagger.ContextModule
import com.andrehaueisen.cronicalia.a_application.dagger.DaggerApplicationComponent
import com.andrehaueisen.cronicalia.models.User
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
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
        val user = User()

        val globalProgressBroadcastChannel = ArrayBroadcastChannel<Int?>(6)
        val globalProgressReceiver = globalProgressBroadcastChannel.openSubscription()

        val globalFileIdUrlBroadcastChannel = ArrayBroadcastChannel<Pair<FileUrlId, Uri>>(capacity = 5)
        val globalFileIdUrlReceiver = globalFileIdUrlBroadcastChannel.openSubscription()

        mComponent = DaggerApplicationComponent.builder()
            .applicationModule(ApplicationModule(
                storageReference,
                databaseReference,
                FirebaseAuth.getInstance(),
                globalProgressBroadcastChannel,
                globalProgressReceiver,
                globalFileIdUrlBroadcastChannel,
                globalFileIdUrlReceiver,
                user))
            .contextModule(ContextModule(this))
            .build()
    }

    fun getAppComponent(): ApplicationComponent = mComponent
}