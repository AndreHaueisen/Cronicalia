package com.andrehaueisen.cronicalia.c_creations.mvp

import com.andrehaueisen.cronicalia.b_firebase.DataRepository
import com.andrehaueisen.cronicalia.b_firebase.FileRepository
import com.andrehaueisen.cronicalia.models.Book
import kotlinx.coroutines.experimental.channels.SubscriptionReceiveChannel

/**
 * Created by andre on 2/19/2018.
 */
class MyCreationsModel(private val mFileRepository: FileRepository, private val mDataRepository: DataRepository){

    enum class SimpleUpdateVariable{
        TITLE, SYNOPSIS, PERIODICITY, GENRE
    }

    fun updateBookOnDatabase(book: Book){
        mDataRepository.setBookDocuments(book, sendProgressUpdate = false)
    }

    fun simpleUpdateBook(newValue: String, collectionLocation: String, bookKey: String, variableToUpdate: SimpleUpdateVariable){

        when(variableToUpdate){
            SimpleUpdateVariable.TITLE -> mDataRepository.updateBookTitle(newValue, collectionLocation, bookKey)
            SimpleUpdateVariable.SYNOPSIS -> mDataRepository.updateBookSynopsis(newValue, collectionLocation, bookKey)
            SimpleUpdateVariable.GENRE -> mDataRepository.updateBookGenre(newValue, collectionLocation, bookKey)
            SimpleUpdateVariable.PERIODICITY -> mDataRepository.updateBookPeriodicity(newValue, collectionLocation, bookKey)
        }
    }

    suspend fun updateBookPoster(book: Book): SubscriptionReceiveChannel<Int?> {
        return mFileRepository.updateBookPoster(book, mDataRepository)
    }

    suspend fun updateBookCover(book: Book): SubscriptionReceiveChannel<Int?>{
        return mFileRepository.updateBookCover(book, mDataRepository)
    }
}