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

        val applicationModule: Module = org.koin.dsl.module.applicationContext {

            bean { FileRepository(storageInstance, globalUser) }
            bean { DataRepository(databaseInstance, globalUser) }
            bean { Authenticator(get(), databaseInstance, FirebaseAuth.getInstance(), globalUser) }
            bean { globalUser }

        }

        startKoin(this, listOf(applicationModule) )
    }

}