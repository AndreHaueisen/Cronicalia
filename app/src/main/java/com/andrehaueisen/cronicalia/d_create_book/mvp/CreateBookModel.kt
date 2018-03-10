package com.andrehaueisen.cronicalia.d_create_book.mvp

import com.andrehaueisen.cronicalia.b_firebase.DataRepository
import com.andrehaueisen.cronicalia.b_firebase.FileRepository
import com.andrehaueisen.cronicalia.models.Book
import kotlinx.coroutines.experimental.channels.SubscriptionReceiveChannel

/**
 * Created by andre on 3/5/2018.
 */
class CreateBookModel(val fileRepository: FileRepository, val dataRepository: DataRepository) {


    suspend fun uploadBookFiles(book: Book): SubscriptionReceiveChannel<Double?> {

        return fileRepository.createBook(book, dataRepository)

    }

    fun getUser() = dataRepository.getUser()


}