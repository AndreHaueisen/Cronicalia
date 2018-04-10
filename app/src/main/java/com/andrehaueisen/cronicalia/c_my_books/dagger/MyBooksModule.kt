package com.andrehaueisen.cronicalia.c_my_books.dagger

import com.andrehaueisen.cronicalia.b_firebase.DataRepository
import com.andrehaueisen.cronicalia.b_firebase.FileRepository
import com.andrehaueisen.cronicalia.c_my_books.mvp.MyBooksModel
import dagger.Module
import dagger.Provides

/**
 * Created by andre on 3/13/2018.
 */
@Module
class MyBooksModule {

    @MyBooksScope
    @Provides
    fun provideMyCreationModel(fileRepository: FileRepository,  dataRepository: DataRepository) = MyBooksModel(fileRepository, dataRepository)
}