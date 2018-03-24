package com.andrehaueisen.cronicalia.c_creations

import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.andrehaueisen.cronicalia.R
import com.andrehaueisen.cronicalia.c_creations.mvp.MyCreationsViewFragment
import com.andrehaueisen.cronicalia.models.Book
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

/**
 * Created by andre on 2/21/2018.
 */
class MyCreationsAdapter(private val mFragment: MyCreationsViewFragment, private val mMyBooks: ArrayList<Book>) :
    RecyclerView.Adapter<MyCreationsAdapter.CreationHolder>() {

    interface CreationClickListener{
        fun onCreationClick(bookKey: String)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CreationHolder {
        val creationView = LayoutInflater.from(mFragment.context).inflate(R.layout.item_book, parent, false)
        return CreationHolder(creationView)
    }

    override fun getItemCount(): Int {
        return mMyBooks.count()
    }

    override fun onBindViewHolder(holder: CreationHolder, position: Int) {
        holder.bindBooksToViews(mMyBooks[position])
    }

    fun updateData(books: ArrayList<Book>) {

        if (mMyBooks.size != books.size) {
            mMyBooks.clear()
            mMyBooks.addAll(books)
            notifyDataSetChanged()
            Log.d("MyCreationsAdapter", "Data set changed")
            return
        }

        mMyBooks.forEachIndexed { index, book ->
            if (book != books[index]) {
                mMyBooks[index] = books[index]
                notifyItemChanged(index)
                Log.d("MyCreationsAdapter", "Data changed at index $index")
            }
        }
    }

    inner class CreationHolder(creationView: View) : RecyclerView.ViewHolder(creationView) {

        private val mItemView = creationView.findViewById<CardView>(R.id.my_creations_card_view)
        private val mBookCoverImageView = creationView.findViewById<ImageView>(R.id.book_cover_image_view)
        private val mBookPosterImageView = creationView.findViewById<ImageView>(R.id.book_poster_image_view)
        private val mBookTitleTextView = creationView.findViewById<TextView>(R.id.title_text_view)
        private val mBookSynopsisTextView = creationView.findViewById<TextView>(R.id.synopsis_text_view)
        private val mBookStatusTextView = creationView.findViewById<TextView>(R.id.book_status_text_view)
        private val mReadingsTextView = creationView.findViewById<TextView>(R.id.readings_text_view)
        private val mRatingTextView = creationView.findViewById<TextView>(R.id.rating_text_view)
        private val mIncomeTextView = creationView.findViewById<TextView>(R.id.income_text_view)

        fun bindBooksToViews(book: Book) {

            mItemView.setOnClickListener {
                mFragment.onCreationClick(bookKey = book.generateBookKey())
            }

            if (book.remoteCoverUri != null) {
                val errorRequestOptions = RequestOptions.errorOf(R.drawable.cover_placeholder)
                val placeHolderRequestOptions = RequestOptions.placeholderOf(R.drawable.cover_placeholder)

                Glide.with(mFragment.context!!)
                    .load(book.remoteCoverUri)
                    .apply(errorRequestOptions)
                    .apply(placeHolderRequestOptions)
                    .into(mBookCoverImageView)
            }

            if (book.remotePosterUri != null) {
                val errorRequestOptions = RequestOptions.errorOf(R.drawable.cover_placeholder)
                val placeHolderRequestOptions = RequestOptions.placeholderOf(R.drawable.cover_placeholder)

                Glide.with(mFragment.context!!)
                    .load(book.remotePosterUri)
                    .apply(errorRequestOptions)
                    .apply(placeHolderRequestOptions)
                    .into(mBookPosterImageView)
            }

            mReadingsTextView.text = mFragment.getString(R.string.simple_number_integer, book.readingNumber)
            mBookTitleTextView.text = book.title
            mBookSynopsisTextView.text = book.synopsis
            if (book.isComplete) {
                mBookStatusTextView.text = mFragment.getString(R.string.book_is_complete)
            } else {
                mBookStatusTextView.text = mFragment.resources.getQuantityString(
                    R.plurals.chapter_number,
                    book.remoteMapChapterUriTitle.size,
                    book.remoteMapChapterUriTitle.size
                )
            }

            mRatingTextView.text = mFragment.getString(R.string.simple_number_float, book.rating)
            mIncomeTextView.text = mFragment.getString(R.string.income_amount, book.income)
        }
    }
}