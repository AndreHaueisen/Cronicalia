package com.andrehaueisen.cronicalia.c_creations.mvp

import com.andrehaueisen.cronicalia.b_firebase.DataRepository
import com.andrehaueisen.cronicalia.b_firebase.FileRepository
import com.andrehaueisen.cronicalia.models.Book
import kotlinx.coroutines.experimental.channels.SubscriptionReceiveChannel

/**
 * Created by andre on 2/19/2018.
 */
class MyCreationsModel(private val mFileRepository: FileRepository, private val mDataRepository: DataRepository){

    fun updateBookOnDatabase(book: Book){
        mDataRepository.setBookDocuments(book, sendProgressUpdate = false)
    }

    suspend fun updateBookPoster(book: Book): SubscriptionReceiveChannel<Int?> {
        return mFileRepository.updateBookPoster(book, mDataRepository)
    }

    suspend fun updateBookCover(book: Book): SubscriptionReceiveChannel<Int?>{
        return mFileRepository.updateBookCover(book, mDataRepository)
    }
}