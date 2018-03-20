package com.andrehaueisen.cronicalia.c_creations.mvp

import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.Fragment
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.TextView
import com.andrehaueisen.cronicalia.R
import com.andrehaueisen.cronicalia.models.Book

/**
 * Created by andre on 3/19/2018.
 */
class MyCreationEditViewFragment: Fragment(), MyCreationsPresenterActivity.PresenterActivity {

    private lateinit var mBook: Book

    private lateinit var mPosterImageView: ImageView
    private lateinit var mCoverImageView: ImageView
    private lateinit var mTitleTextView: TextView
    private lateinit var mSynopsisTextView: TextView
    private lateinit var mBookStatusRadioGroup: RadioGroup
    private lateinit var mGenreSpinner: Spinner
    private lateinit var mPeriodicitySpinner: Spinner
    private lateinit var mChaptersRecyclerView: RecyclerView
    private lateinit var mAddChapterFab: FloatingActionButton
    private lateinit var mDeleteBookFab: FloatingActionButton

    companion object {

        fun newInstance(bundle: Bundle? = null): MyCreationEditViewFragment {

            val fragment = MyCreationEditViewFragment()
            bundle?.let {
                fragment.arguments = bundle
            }

            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.c_fragment_my_creation_edit, container, false)

        mPosterImageView = view.findViewById(R.id.poster_image_view)
        mCoverImageView = view.findViewById(R.id.cover_image_view)
        mTitleTextView = view.findViewById(R.id.title_text_view)
        mSynopsisTextView = view.findViewById(R.id.synopsis_text_view)
        mBookStatusRadioGroup = view.findViewById(R.id.book_launch_status_radio_group)
        mGenreSpinner = view.findViewById(R.id.genre_spinner)
        mPeriodicitySpinner = view.findViewById(R.id.periodicity_spinner)
        mChaptersRecyclerView = view.findViewById(R.id.chapters_recycler_view)
        mAddChapterFab = view.findViewById(R.id.add_chapter_fab)
        mDeleteBookFab = view.findViewById(R.id.delete_book_fab)

        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun refreshFragmentData(book: Book) {
        mBook = book
    }
}