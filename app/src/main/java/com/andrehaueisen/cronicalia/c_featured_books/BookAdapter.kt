package com.andrehaueisen.cronicalia.c_featured_books

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.andrehaueisen.cronicalia.R
import com.andrehaueisen.cronicalia.models.Book
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

class BookAdapter(val context: Context, val bookCollection: List<Book>): RecyclerView.Adapter<BookAdapter.BookViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {

        val bookView = LayoutInflater.from(context).inflate(R.layout.item_book, parent, false)

        return BookViewHolder(bookView)
    }

    override fun getItemCount(): Int {
        return bookCollection.size
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        holder.bindBookToView()
    }

    inner class BookViewHolder(bookView: View): RecyclerView.ViewHolder(bookView){

        private val mBookCoverImageView = bookView.findViewById<ImageView>(R.id.cover_image_view)
        private val mBookTitleTextView = bookView.findViewById<TextView>(R.id.title_text_view)
        private val mBookAuthorTextView = bookView.findViewById<TextView>(R.id.author_name_text_view)
        private val mBookSynopsisTextView = bookView.findViewById<TextView>(R.id.synopsis_text_view)
        private val mReadingsTextView = bookView.findViewById<TextView>(R.id.readings_text_view)
        private val mRatingTextView = bookView.findViewById<TextView>(R.id.rating_text_view)
        private val mIncomeTextView = bookView.findViewById<TextView>(R.id.income_text_view)

        internal fun bindBookToView(){
            val book = bookCollection[layoutPosition]

            loadImage(book)
            mBookTitleTextView.text = book.title
            mBookAuthorTextView.text = book.authorName
            mBookSynopsisTextView.text = book.synopsis
            mReadingsTextView.text = context.getString(R.string.simple_number_integer, book.readingNumber)
            mRatingTextView.text = context.getString(R.string.simple_number_float, book.rating)
            mIncomeTextView.text = context.getString(R.string.income_amount, book.income)

        }

        private fun loadImage(book: Book){
            val requestOptions = RequestOptions.errorOf(R.drawable.poster_placeholder)

            Glide.with(context)
                .load(book.remoteCoverUri)
                .apply(requestOptions)
                .into(mBookCoverImageView)
        }
    }
}