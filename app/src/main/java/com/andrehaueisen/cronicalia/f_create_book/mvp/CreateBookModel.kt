package com.andrehaueisen.cronicalia.f_create_book.mvp

import com.andrehaueisen.cronicalia.b_firebase.DataRepository
import com.andrehaueisen.cronicalia.b_firebase.FileRepository
import com.andrehaueisen.cronicalia.models.Book
import kotlinx.coroutines.experimental.channels.SubscriptionReceiveChannel

/**
 * Created by andre on 3/5/2018.
 */
class CreateBookModel(private val fileRepository: FileRepository, private val dataRepository: DataRepository) {


    suspend fun uploadBookFiles(book: Book): SubscriptionReceiveChannel<Int> {

        return fileRepository.createBook(book, dataRepository)
    }

    fun getUser() = dataRepository.getUser()


}