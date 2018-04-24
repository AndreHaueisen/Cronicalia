package com.andrehaueisen.cronicalia.f_my_books.mvp

import android.support.v4.util.ArraySet
import com.andrehaueisen.cronicalia.b_firebase.DataRepository
import com.andrehaueisen.cronicalia.b_firebase.FileRepository
import com.andrehaueisen.cronicalia.models.Book
import kotlinx.coroutines.experimental.channels.SubscriptionReceiveChannel

/**
 * Created by andre on 2/19/2018.
 */
class MyBooksModel(private val mFileRepository: FileRepository, private val mDataRepository: DataRepository){

    enum class SimpleUpdateVariable{
        TITLE, SYNOPSIS, PERIODICITY, GENRE
    }

    suspend fun updateBookPdfsReferences(book: Book): Int{
        return mDataRepository.updateBookPdfReferences(book)
    }

    fun deleteBook(book: Book){
        mFileRepository.deleteFullBook(book, dataRepository = mDataRepository)
    }

    fun simpleUpdateBook(newValue: String, collectionLocation: String, bookKey: String, variableToUpdate: SimpleUpdateVariable){

        when(variableToUpdate){
            SimpleUpdateVariable.TITLE -> mDataRepository.updateBookTitle(newValue, collectionLocation, bookKey)
            SimpleUpdateVariable.SYNOPSIS -> mDataRepository.updateBookSynopsis(newValue, collectionLocation, bookKey)
            SimpleUpdateVariable.GENRE -> mDataRepository.updateBookGenre(newValue, collectionLocation, bookKey)
            SimpleUpdateVariable.PERIODICITY -> mDataRepository.updateBookPeriodicity(newValue, collectionLocation, bookKey)
        }
    }

    suspend fun updateBookPoster(book: Book): Int {
        return mFileRepository.updateBookPoster(book, mDataRepository)
    }

    suspend fun updateBookCover(book: Book): Int{
        return mFileRepository.updateBookCover(book, mDataRepository)
    }

    suspend fun updateBookPdfs(book: Book, filesToBeDeleted: ArraySet<String>): SubscriptionReceiveChannel<Int>{
        return mFileRepository.updatePdfs(book, mDataRepository,  filesToBeDeleted)
    }
}