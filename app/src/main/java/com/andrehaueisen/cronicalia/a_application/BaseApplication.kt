package com.andrehaueisen.cronicalia.a_application

import android.app.Application
import com.andrehaueisen.cronicalia.b_firebase.Authenticator
import com.andrehaueisen.cronicalia.b_firebase.DataRepository
import com.andrehaueisen.cronicalia.b_firebase.FileRepository
import com.andrehaueisen.cronicalia.models.User
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.experimental.channels.ArrayBroadcastChannel
import org.koin.android.ext.android.startKoin
import org.koin.dsl.module.Module


/**
 * Created by andre on 2/18/2018.
 */
class BaseApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()

        FirebaseApp.initializeApp(this)
        val storageInstance = FirebaseStorage.getInstance()
        val databaseInstance = FirebaseFirestore.getInstance()
        databaseInstance.firestoreSettings = settings
        val globalUser = User()

        val globalProgressBroadcastChannel = ArrayBroadcastChannel<Int?>(4)
        val globalProgressReceiver = globalProgressBroadcastChannel.openSubscription()

       /* val contextModule: Module = org.koin.dsl.module.applicationContext{
            bean { this@BaseApplication.applicationContext }
        }*/

        val applicationModule: Module = org.koin.dsl.module.applicationContext {

            bean { FileRepository(storageInstance, globalProgressBroadcastChannel, globalProgressReceiver, globalUser) }
            bean { DataRepository(databaseInstance, globalProgressBroadcastChannel, globalProgressReceiver, globalUser) }
            bean { Authenticator(get(), databaseInstance, FirebaseAuth.getInstance(), globalUser) }
            bean { globalUser }

        }

        startKoin(this, listOf(/*contextModule,*/ applicationModule))

    }

}