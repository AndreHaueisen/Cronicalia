package com.andrehaueisen.cronicalia.d_create_book.dagger

import com.andrehaueisen.cronicalia.b_firebase.DataRepository
import com.andrehaueisen.cronicalia.b_firebase.FileRepository
import com.andrehaueisen.cronicalia.d_create_book.mvp.CreateBookModel
import dagger.Module
import dagger.Provides

/**
 * Created by andre on 3/5/2018.
 */
@Module
class CreateBookModule {

    @CreateBookScope
    @Provides
    fun provideModel(fileRepository: FileRepository, dataRepository: DataRepository) =
        CreateBookModel(fileRepository, dataRepository)
}