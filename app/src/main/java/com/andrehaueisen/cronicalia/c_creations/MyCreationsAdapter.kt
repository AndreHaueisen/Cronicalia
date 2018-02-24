package com.andrehaueisen.cronicalia.c_creations

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

/**
 * Created by andre on 2/21/2018.
 */
class MyCreationsAdapter(private val mContext: Context, private val mMyBooks: ArrayList<Book>): RecyclerView.Adapter<MyCreationsAdapter.CreationHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): CreationHolder {
        val creationView = LayoutInflater.from(mContext).inflate(R.layout.item_book, parent, false)
        return CreationHolder(creationView)
    }

    override fun getItemCount(): Int {
        return mMyBooks.count()
    }

    override fun onBindViewHolder(holder: CreationHolder, position: Int) {
        holder.bindBooksToViews(mMyBooks[position])
    }

    inner class CreationHolder(creationView: View): RecyclerView.ViewHolder(creationView){

        private val mBookCoverImageView = creationView.findViewById<ImageView>(R.id.book_cover_image_view)
        private val mBookTitleTextView = creationView.findViewById<TextView>(R.id.title_text_view)
        private val mBookStatusTextView = creationView.findViewById<TextView>(R.id.book_status_text_view)
        private val mReadingsTextView = creationView.findViewById<TextView>(R.id.readings_text_view)
        private val mRatingTextView = creationView.findViewById<TextView>(R.id.rating_text_view)
        private val mIncomeTextView = creationView.findViewById<TextView>(R.id.income_text_view)

        fun bindBooksToViews(book: Book){

            Glide.with(mContext).load(R.drawable.cover_placeholder).into(mBookCoverImageView)
            mReadingsTextView.text = mContext.getString(R.string.simple_number_integer, book.readingNumber)
            mBookTitleTextView.text = book.title
            if(book.isComplete){
                mBookStatusTextView.text = mContext.getString(R.string.book_is_complete)
            } else {
                mBookStatusTextView.text = mContext.resources.getQuantityString(R.plurals.chapter_number, book.chaptersTitles.size, book.chaptersTitles.size)
            }

            mRatingTextView.text = mContext.getString(R.string.simple_number_float, book.rating)
            mIncomeTextView.text = mContext.getString(R.string.income_amount, book.income)
        }
    }
}