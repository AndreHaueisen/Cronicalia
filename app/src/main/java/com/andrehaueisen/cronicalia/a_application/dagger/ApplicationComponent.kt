package com.andrehaueisen.cronicalia.a_application.dagger

import com.andrehaueisen.cronicalia.b_firebase.DataRepository
import com.andrehaueisen.cronicalia.b_firebase.FileRepository
import dagger.Component

/**
 * Created by andre on 2/18/2018.
 */
@ApplicationScope
@Component(modules = [ContextModule::class, ApplicationModule::class])
interface ApplicationComponent {

    fun loadFileRepository() : FileRepository
    fun loadDataRepository() : DataRepository
}