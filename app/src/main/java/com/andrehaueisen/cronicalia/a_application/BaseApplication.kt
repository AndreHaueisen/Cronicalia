package com.andrehaueisen.cronicalia.a_application

import android.app.Activity
import android.app.Application
import com.andrehaueisen.cronicalia.a_application.dagger.ApplicationComponent
import com.andrehaueisen.cronicalia.a_application.dagger.ApplicationModule
import com.andrehaueisen.cronicalia.a_application.dagger.ContextModule
import com.andrehaueisen.cronicalia.models.User
import com.google.firebase.FirebaseApp
import com.google.firebase.storage.FirebaseStorage

/**
 * Created by andre on 2/18/2018.
 */
class BaseApplication : Application() {

    lateinit private var mComponent : ApplicationComponent

    companion object{
        fun get(activity: Activity): BaseApplication = activity.application as BaseApplication
    }

    override fun onCreate() {
        super.onCreate()

        FirebaseApp.initializeApp(this)
        val storageReference = FirebaseStorage.getInstance().reference
        val user = User()

        mComponent = DaggerApplicationComponent.builder()
            .applicationModule(ApplicationModule(storageReference, user))
            .contextModule(ContextModule(this))
            .build()
    }

    fun getAppComponent(): ApplicationComponent = mComponent
}