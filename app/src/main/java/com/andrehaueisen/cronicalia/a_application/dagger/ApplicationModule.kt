package com.andrehaueisen.cronicalia.a_application.dagger

import com.andrehaueisen.cronicalia.b_firebase.FileRepository
import com.andrehaueisen.cronicalia.models.User
import com.google.firebase.storage.StorageReference
import dagger.Module
import dagger.Provides

/**
 * Created by andre on 2/18/2018.
 */
@Module
class ApplicationModule(val mStorageReference: StorageReference, val mUser: User) {

    @ApplicationScope
    @Provides
    fun provideFileRepository() = FileRepository(mStorageReference)
}