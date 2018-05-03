package com.andrehaueisen.cronicalia.c_featured_books.mvp

import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.andrehaueisen.cronicalia.PARCELABLE_BOOK_OPINIONS
import com.andrehaueisen.cronicalia.PARCELABLE_SELECTED_BOOK
import com.andrehaueisen.cronicalia.R
import com.andrehaueisen.cronicalia.models.Book
import com.andrehaueisen.cronicalia.models.BookOpinion
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import java.util.*


class SelectedBookFragment : Fragment(), FeaturedBooksPresenterActivity.FeaturedBooksPresenterInterface {

    private lateinit var mBookPosterImageView: ImageView
    private lateinit var mBookCoverImageView: ImageView
    private lateinit var mBookTitleTextView: TextView
    private lateinit var mBookGenreTextView: TextView
    private lateinit var mAuthorNameTextView: TextView
    private lateinit var mTwitterAccountTextView: TextView
    private lateinit var mPublicationDateTextView: TextView
    private lateinit var mRatingTextView: TextView
    private lateinit var mReadingsTextView: TextView
    private lateinit var mIncomeTextView: TextView
    private lateinit var mSynopsisTextView: TextView

    private lateinit var mSelectedBook: Book
    private val mBookOpinions = ArrayList<BookOpinion>()

    companion object {

        fun newInstance(bundle: Bundle? = null): SelectedBookFragment {

            val fragment = SelectedBookFragment()
            bundle?.let {
                fragment.arguments = bundle
            }
            return fragment
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {

        outState.putParcelable(PARCELABLE_SELECTED_BOOK, mSelectedBook)
        outState.putParcelableArrayList(PARCELABLE_BOOK_OPINIONS, mBookOpinions)

        super.onSaveInstanceState(outState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.c_fragment_selected_book, container, false)

        mBookPosterImageView = view.findViewById(R.id.book_poster_image_view)
        mBookCoverImageView = view.findViewById(R.id.book_cover_image_view)
        mBookTitleTextView = view.findViewById(R.id.book_title_text_view)
        mBookGenreTextView = view.findViewById(R.id.genre_text_view)
        mAuthorNameTextView = view.findViewById(R.id.author_name_text_view)
        mTwitterAccountTextView = view.findViewById(R.id.twitter_account_text_view)
        mPublicationDateTextView = view.findViewById(R.id.publication_date_text_view)
        mRatingTextView = view.findViewById(R.id.rating_text_view)
        mReadingsTextView = view.findViewById(R.id.readings_text_view)
        mIncomeTextView = view.findViewById(R.id.income_text_view)
        mSynopsisTextView = view.findViewById(R.id.synopsis_text_view)

        if(savedInstanceState != null){
            mSelectedBook = savedInstanceState.getParcelable(PARCELABLE_SELECTED_BOOK)
            mBookOpinions.clear()
            mBookOpinions.addAll(savedInstanceState.getParcelableArrayList(PARCELABLE_BOOK_OPINIONS))
        }

        initializeImages()
        initializeTexts()

        return view
    }

    private fun initializeImages() {

        val transitionOptions = DrawableTransitionOptions.withCrossFade()

        if (mSelectedBook.remotePosterUri != null) {
            Glide.with(this)
                .load(Uri.parse(mSelectedBook.remotePosterUri))
                .error(Glide.with(this).load(R.drawable.poster_placeholder))
                .into(mBookPosterImageView)
        }

        if (mSelectedBook.remoteCoverUri != null) {
            Glide.with(this)
                .load(Uri.parse(mSelectedBook.remoteCoverUri))
                .error(Glide.with(this).load(R.drawable.cover_placeholder))
                .transition(transitionOptions)
                .into(mBookCoverImageView)
        }

    }

    private fun initializeTexts() {
        mBookTitleTextView.text = mSelectedBook.title
        mBookGenreTextView.text = mSelectedBook.genre.name
        mAuthorNameTextView.text = mSelectedBook.authorName
        if (mSelectedBook.authorTwitterAccount != null)
            mTwitterAccountTextView.text = getString(R.string.twitter_account, mSelectedBook.authorTwitterAccount)
        else {
            mTwitterAccountTextView.visibility = View.GONE
        }
        mRatingTextView.text = getString(R.string.simple_number_float, mSelectedBook.rating)
        mReadingsTextView.text = getString(R.string.simple_number_integer, mSelectedBook.readingsNumber)
        mIncomeTextView.text = getString(R.string.income_amount, mSelectedBook.income)
        mSynopsisTextView.text = mSelectedBook.synopsis

        if(mSelectedBook.publicationDate != 0L) {
            mPublicationDateTextView.text = getString(R.string.publication_date, mSelectedBook.convertRawDateToString(resources))
        }
    }

    override fun setInitialData(selectedBook: Book, newBookOpinions: ArrayList<BookOpinion>){
        mSelectedBook = selectedBook
        mBookOpinions.clear()
        mBookOpinions.addAll(newBookOpinions)
    }

    override fun refreshFragmentData(selectedBook: Book, newBookOpinions: ArrayList<BookOpinion>) {
        mSelectedBook = selectedBook
        mBookOpinions.clear()
        mBookOpinions.addAll(newBookOpinions)

        initializeImages()
        initializeTexts()
    }
}