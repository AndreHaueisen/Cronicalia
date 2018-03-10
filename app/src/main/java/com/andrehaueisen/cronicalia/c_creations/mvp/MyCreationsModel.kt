package com.andrehaueisen.cronicalia.c_creations.mvp

import com.andrehaueisen.cronicalia.AUTHOR_NAME_PLACE_HOLDER
import com.andrehaueisen.cronicalia.ENCODED_EMAIL_PLACE_HOLDER
import com.andrehaueisen.cronicalia.models.Book

/**
 * Created by andre on 2/19/2018.
 */
class MyCreationsModel {

    private val mFakeChaptersTitles = ArrayList<String>(4)

    init {
        mFakeChaptersTitles.add("Chapter One")
        mFakeChaptersTitles.add("Chapter Two")
        mFakeChaptersTitles.add("Chapter Three")
        mFakeChaptersTitles.add("Chapter Four")
    }

    fun createFakeBooks(): ArrayList<Book>{
        val fakeBooks = ArrayList<Book>(3)
        val book1 = Book(
            "The good fella",
            AUTHOR_NAME_PLACE_HOLDER,
            ENCODED_EMAIL_PLACE_HOLDER,
            Book.BookGenre.COMEDY,
            5.5F,
            10,
            250F,
            10000,
            Book.BookLanguage.ENGLISH)

        val book2 = Book(
            "When the sun hit the flor",
            AUTHOR_NAME_PLACE_HOLDER,
            ENCODED_EMAIL_PLACE_HOLDER,
            Book.BookGenre.FICTION,
            9.5F,
            1500,
            500.50F,
            5700000,
            Book.BookLanguage.ENGLISH)

        val book3 = Book(
            "The dead dad",
            AUTHOR_NAME_PLACE_HOLDER,
            ENCODED_EMAIL_PLACE_HOLDER,
            Book.BookGenre.HORROR,
            7.5F,
            2500,
            0.75F,
            2010000,
            Book.BookLanguage.ENGLISH)

        fakeBooks.add(book1)
        fakeBooks.add(book2)
        fakeBooks.add(book3)

        return fakeBooks
    }
}