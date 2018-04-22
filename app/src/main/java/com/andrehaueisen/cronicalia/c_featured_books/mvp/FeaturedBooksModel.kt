package com.andrehaueisen.cronicalia.c_featured_books.mvp

import com.andrehaueisen.cronicalia.b_firebase.DataRepository
import com.andrehaueisen.cronicalia.models.Book

class FeaturedBooksModel(private val mDataRepository: DataRepository) {


    suspend fun getFeaturedBooks(queryingGenre: Book.BookGenre): List<Book>{
        val bookLanguage = Book.BookLanguage.ENGLISH

        return when(queryingGenre){
            Book.BookGenre.ACTION -> mDataRepository.queryFeaturedActionBooks(bookLanguage)
            Book.BookGenre.FICTION -> mDataRepository.queryFeaturedFictionBooks(bookLanguage)
            Book.BookGenre.ROMANCE -> mDataRepository.queryFeaturedRomanceBooks(bookLanguage)
            Book.BookGenre.COMEDY -> mDataRepository.queryFeaturedComedyBooks(bookLanguage)
            Book.BookGenre.DRAMA -> mDataRepository.queryFeaturedDramaBooks(bookLanguage)
            Book.BookGenre.HORROR -> mDataRepository.queryFeaturedHorrorBooks(bookLanguage)
            Book.BookGenre.SATIRE -> mDataRepository.queryFeaturedSatireBooks(bookLanguage)
            Book.BookGenre.FANTASY -> mDataRepository.queryFeaturedFantasyBooks(bookLanguage)
            Book.BookGenre.MYTHOLOGY -> mDataRepository.queryFeaturedMythologyBooks(bookLanguage)
            Book.BookGenre.ADVENTURE -> mDataRepository.queryFeaturedAdventureBooks(bookLanguage)
            Book.BookGenre.UNDEFINED -> mDataRepository.queryFeaturedActionBooks(bookLanguage)
        }

    }





}