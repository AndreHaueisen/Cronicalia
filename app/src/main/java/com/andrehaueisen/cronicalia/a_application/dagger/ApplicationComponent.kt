package com.andrehaueisen.cronicalia.a_application.dagger

import com.andrehaueisen.cronicalia.b_firebase.Authenticator
import com.andrehaueisen.cronicalia.b_firebase.DataRepository
import com.andrehaueisen.cronicalia.b_firebase.FileRepository
import com.andrehaueisen.cronicalia.models.User
import dagger.Component

/**
 * Created by andre on 2/18/2018.
 */
@ApplicationScope
@Component(modules = [ContextModule::class, ApplicationModule::class])
interface ApplicationComponent {

    fun loadFileRepository() : FileRepository
    fun loadDataRepository() : DataRepository
    fun loadAuthenticator() : Authenticator
    fun loadUser() : User
}