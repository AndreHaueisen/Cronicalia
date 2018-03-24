package com.andrehaueisen.cronicalia.c_creations.dagger

import com.andrehaueisen.cronicalia.b_firebase.DataRepository
import com.andrehaueisen.cronicalia.b_firebase.FileRepository
import com.andrehaueisen.cronicalia.c_creations.mvp.MyCreationsModel
import dagger.Module
import dagger.Provides

/**
 * Created by andre on 3/13/2018.
 */
@Module
class MyCreationsModule {

    @MyCreationsScope
    @Provides
    fun provideMyCreationModel(fileRepository: FileRepository,  dataRepository: DataRepository) = MyCreationsModel(fileRepository, dataRepository)
}