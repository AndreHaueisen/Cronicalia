package com.andrehaueisen.cronicalia.f_my_books

import android.support.constraint.ConstraintLayout
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.andrehaueisen.cronicalia.R
import com.andrehaueisen.cronicalia.f_my_books.mvp.MyBooksViewFragment
import com.andrehaueisen.cronicalia.models.Book
import com.andrehaueisen.cronicalia.utils.extensions.convertDpToPixel
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions



/**
 * Created by andre on 2/21/2018.
 */
class MyBooksAdapter(private val mFragment: MyBooksViewFragment, private val mMyBooks: ArrayList<Book>) :
    RecyclerView.Adapter<MyBooksAdapter.BookHolder>() {

    interface BookClickListener{
        fun onBookClick(bookKey: String)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookHolder {
        val creationView = LayoutInflater.from(mFragment.context).inflate(R.layout.item_my_book, parent, false)
        return BookHolder(creationView)
    }

    override fun getItemCount(): Int {
        return mMyBooks.count()
    }

    override fun onBindViewHolder(holder: BookHolder, position: Int) {
        holder.bindBooksToViews(mMyBooks[position])
    }

    fun updateData(books: ArrayList<Book>) {

        if (mMyBooks.size != books.size) {
            mMyBooks.clear()
            mMyBooks.addAll(books)
            notifyDataSetChanged()
            Log.d("MyBooksAdapter", "Data set changed")
            return
        }

        mMyBooks.forEachIndexed { index, book ->
            if (book != books[index]) {
                mMyBooks[index] = books[index]
                notifyItemChanged(index)
                Log.d("MyBooksAdapter", "Data changed at index $index")
            }
        }
    }

    inner class BookHolder(bookView: View) : RecyclerView.ViewHolder(bookView) {

        private val mOpinionsButton = bookView.findViewById<Button>(R.id.view_opinions_button)
        private val mScrollIndicatorView = bookView.findViewById<View>(R.id.horizontal_scroll_indicator)
        private val mEditButton = bookView.findViewById<Button>(R.id.edit_button)
        private val mBookCoverImageView = bookView.findViewById<ImageView>(R.id.book_cover_image_view)
        private val mBookTitleTextView = bookView.findViewById<TextView>(R.id.title_text_view)
        private val mBookSynopsisTextView = bookView.findViewById<TextView>(R.id.synopsis_text_view)
        private val mBookStatusTextView = bookView.findViewById<TextView>(R.id.book_status_text_view)
        private val mPublicationTextView = bookView.findViewById<TextView>(R.id.publication_date_text_view)
        private val mReadingsTextView = bookView.findViewById<TextView>(R.id.readings_text_view)
        private val mRatingTextView = bookView.findViewById<TextView>(R.id.rating_text_view)
        private val mIncomeTextView = bookView.findViewById<TextView>(R.id.income_text_view)

        fun bindBooksToViews(book: Book) {

            mEditButton.setOnClickListener {
                mFragment.onBookClick(bookKey = book.generateBookKey())
            }

            if (book.remoteCoverUri != null) {
                val errorRequestOptions = RequestOptions.errorOf(R.drawable.cover_placeholder)

                Glide.with(mFragment.context!!)
                    .load(book.localCoverUri)
                    .error(Glide.with(mFragment.context!!).load(book.remoteCoverUri))
                    .apply(errorRequestOptions)
                    .into(mBookCoverImageView)
            }

            mReadingsTextView.text = mFragment.getString(R.string.simple_number_integer, book.readingsNumber)
            mBookTitleTextView.text = book.title
            mBookSynopsisTextView.text = book.synopsis
            if (book.isLaunchedComplete) {
                mBookStatusTextView.text = mFragment.getString(R.string.book_is_complete)
            } else {
                mBookStatusTextView.text = mFragment.resources.getQuantityString(
                    R.plurals.chapter_number,
                    book.remoteChapterTitles.size,
                    book.remoteChapterTitles.size
                )
            }

            if(book.publicationDate != 0L){
                mPublicationTextView.text = mFragment.getString(R.string.publication_date, book.convertRawDateToString(mFragment.resources))
            } else {
                mPublicationTextView.visibility = View.GONE
            }

            mRatingTextView.text = mFragment.getString(R.string.simple_number_float, book.rating)
            mIncomeTextView.text = mFragment.getString(R.string.income_amount, book.income)

            if(mMyBooks.count() > 1) {

                when (layoutPosition) {
                    0 -> setViewMargin(mScrollIndicatorView, 48F, 48F)

                    mMyBooks.count() - 1 -> setViewMargin(mScrollIndicatorView, top = 48F, right = 48F)

                    else -> setViewMargin(mScrollIndicatorView, top = 48F)
                }

            } else {
                setViewMargin(mScrollIndicatorView, 48F, 48F, 48F)
            }

        }

        private fun setViewMargin(view: View, left: Float = 0F, top: Float = 0F, right: Float = 0F, bottom: Float = 0F){

            val context = mFragment.requireContext()
            val marginLeft = context.convertDpToPixel(left).toInt()
            val marginTop = context.convertDpToPixel(top).toInt()
            val marginRight = context.convertDpToPixel(right).toInt()
            val marginBottom = context.convertDpToPixel(bottom).toInt()
            val params = view.layoutParams as ConstraintLayout.LayoutParams
            params.setMargins(marginLeft, marginTop, marginRight, marginBottom) //substitute parameters for left, top, right, bottom
            view.layoutParams = params
        }
    }
}