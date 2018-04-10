package com.andrehaueisen.cronicalia.a_application

import android.app.Activity
import android.app.Application
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

    override fun onCreate() {
        super.onCreate()

        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()

        FirebaseApp.initializeApp(this)
        val storageInstance = FirebaseStorage.getInstance()
        val databaseInstance = FirebaseFirestore.getInstance()
        databaseInstance.firestoreSettings = settings
        val user = User()

        val globalProgressBroadcastChannel = ArrayBroadcastChannel<Int?>(4)
        val globalProgressReceiver = globalProgressBroadcastChannel.openSubscription()

        mComponent = DaggerApplicationComponent.builder()
            .applicationModule(ApplicationModule(
                storageInstance,
                databaseInstance,
                FirebaseAuth.getInstance(),
                globalProgressBroadcastChannel,
                globalProgressReceiver,
                user))
            .contextModule(ContextModule(this))
            .build()
    }

    fun getAppComponent(): ApplicationComponent = mComponent
}