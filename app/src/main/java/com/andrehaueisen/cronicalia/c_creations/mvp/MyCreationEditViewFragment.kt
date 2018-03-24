package com.andrehaueisen.cronicalia.c_creations.mvp

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.Fragment
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.andrehaueisen.cronicalia.*
import com.andrehaueisen.cronicalia.c_creations.EditTextDialog
import com.andrehaueisen.cronicalia.models.Book
import com.andrehaueisen.cronicalia.utils.extensions.createBookPictureDirectory
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import es.dmoral.toasty.Toasty
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.channels.consumeEach
import kotlinx.coroutines.experimental.launch
import java.io.File

/**
 * Created by andre on 3/19/2018.
 */
class MyCreationEditViewFragment : Fragment(), MyCreationsPresenterActivity.PresenterActivity, EditTextDialog.BookChangesListener {

    private lateinit var mBook: Book
    private lateinit var mImageDestination: ImageDestination
    private lateinit var mPosterImageView: ImageView
    private lateinit var mCoverImageView: ImageView
    private lateinit var mTitleTextView: TextView
    private lateinit var mSynopsisTextView: TextView
    private lateinit var mReadingsTextView: TextView
    private lateinit var mRatingTextView: TextView
    private lateinit var mIncomeTextView: TextView
    private lateinit var mGenreSpinner: Spinner
    private lateinit var mPeriodicitySpinner: Spinner
    private lateinit var mChaptersRecyclerView: RecyclerView
    private lateinit var mAddChapterFab: FloatingActionButton
    private lateinit var mDeleteBookFab: FloatingActionButton

    private enum class ImageDestination {
        COVER, POSTER
    }

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

        mBook = arguments?.getParcelable(PARCELABLE_BOOK)!!

        mPosterImageView = view.findViewById(R.id.poster_image_view)
        mCoverImageView = view.findViewById(R.id.cover_image_view)
        mTitleTextView = view.findViewById(R.id.title_text_view)
        mSynopsisTextView = view.findViewById(R.id.synopsis_text_view)
        mReadingsTextView = view.findViewById(R.id.readings_text_view)
        mRatingTextView = view.findViewById(R.id.rating_text_view)
        mIncomeTextView = view.findViewById(R.id.income_text_view)
        mGenreSpinner = view.findViewById(R.id.genre_spinner)
        mPeriodicitySpinner = view.findViewById(R.id.periodicity_spinner)
        mChaptersRecyclerView = view.findViewById(R.id.chapters_recycler_view)
        mAddChapterFab = view.findViewById(R.id.add_chapter_fab)
        mDeleteBookFab = view.findViewById(R.id.delete_book_fab)

        if (!mBook.isComplete) {
            mGenreSpinner.visibility = View.VISIBLE
            mPeriodicitySpinner.visibility = View.VISIBLE
            mAddChapterFab.visibility = View.VISIBLE
            mChaptersRecyclerView.visibility = View.VISIBLE
        }

        initiateTitleTextView()
        initiateSynopsisTextView()
        initiateImageViews()
        initiateSpinners()
        bindDataToViews()

        return view
    }

    private fun initiateTitleTextView() {
        mTitleTextView.setOnClickListener {
            EditTextDialog(this, EditTextDialog.ViewBeingEdited.TITLE_VIEW, mBook).show()
        }
    }

    private fun initiateSynopsisTextView() {
        mSynopsisTextView.setOnClickListener {
            EditTextDialog(this, EditTextDialog.ViewBeingEdited.SYNOPSIS_VIEW, mBook).show()
        }
    }

    private fun initiateImageViews() {
        mCoverImageView.setOnClickListener {
            val file: File
            val directoryName: String

            if (mBook != null && mBook?.localCoverUri != null) {
                directoryName = mBook!!.localCoverUri!!.substringBeforeLast("/").substringAfterLast("/")
                file = activity!!
                    .cacheDir
                    .createBookPictureDirectory(directoryName, FILE_NAME_BOOK_COVER)
            } else {
                file = context!!.cacheDir.createBookPictureDirectory("book_0${mBook?.bookPosition}", FILE_NAME_BOOK_COVER)
            }

            CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setActivityTitle(context!!.getString(R.string.cover))
                .setOutputUri(Uri.fromFile(file))
                .setAspectRatio(3, 4)
                .start(context!!, this)

            mImageDestination = ImageDestination.COVER

        }

        mPosterImageView.setOnClickListener {
            val directoryName = mBook?.localCoverUri?.substringBeforeLast("/")?.substringAfterLast("/")

            if (directoryName != null) {
                val file = activity!!
                    .cacheDir
                    .createBookPictureDirectory(directoryName, FILE_NAME_BOOK_POSTER)

                CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setActivityTitle(context!!.getString(R.string.poster))
                    .setAspectRatio(16, 9)
                    .setOutputUri(Uri.fromFile(file))
                    .start(context!!, this)

                mImageDestination = ImageDestination.POSTER
            }
        }
    }

    private fun initiateSpinners() {

        fun setGenreSpinner() {
            val adapter = ArrayAdapter.createFromResource(context, R.array.genre_array, R.layout.item_spinner)
            adapter.setDropDownViewResource(R.layout.item_dropdown_spinner)
            mGenreSpinner.adapter = adapter
            mGenreSpinner.onItemSelectedListener =
                    object : AdapterView.OnItemSelectedListener {
                        override fun onNothingSelected(p0: AdapterView<*>?) {
                            mBook.genre = Book.BookGenre.UNDEFINED
                        }

                        override fun onItemSelected(adapter: AdapterView<*>?, clickedView: View?, itemPosition: Int, id: Long) {

                            when (itemPosition) {
                                0 -> mBook.genre = Book.BookGenre.ACTION
                                1 -> mBook.genre = Book.BookGenre.FICTION
                                2 -> mBook.genre = Book.BookGenre.ROMANCE
                                3 -> mBook.genre = Book.BookGenre.COMEDY
                                4 -> mBook.genre = Book.BookGenre.DRAMA
                                5 -> mBook.genre = Book.BookGenre.HORROR
                                6 -> mBook.genre = Book.BookGenre.SATIRE
                                7 -> mBook.genre = Book.BookGenre.FANTASY
                                8 -> mBook.genre = Book.BookGenre.MYTHOLOGY
                                9 -> mBook.genre = Book.BookGenre.ADVENTURE
                            }
                            notifySimpleChange()
                        }
                    }
        }

        fun setPeriodicitySpinner() {

            val adapter = ArrayAdapter.createFromResource(context, R.array.periodicity_array, R.layout.item_spinner)

            adapter.setDropDownViewResource(R.layout.item_dropdown_spinner)
            mPeriodicitySpinner.adapter = adapter
            mPeriodicitySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(adapter: AdapterView<*>?) {
                    mBook.periodicity = Book.ChapterPeriodicity.NONE
                }

                override fun onItemSelected(adapter: AdapterView<*>?, clickedView: View?, itemPosition: Int, id: Long) {
                    when (itemPosition) {
                        0 -> mBook.periodicity = Book.ChapterPeriodicity.EVERY_DAY
                        1 -> mBook.periodicity = Book.ChapterPeriodicity.EVERY_3_DAYS
                        2 -> mBook.periodicity = Book.ChapterPeriodicity.EVERY_7_DAYS
                        3 -> mBook.periodicity = Book.ChapterPeriodicity.EVERY_14_DAYS
                        4 -> mBook.periodicity = Book.ChapterPeriodicity.EVERY_30_DAYS
                        5 -> mBook.periodicity = Book.ChapterPeriodicity.EVERY_42_DAYS
                    }
                    notifySimpleChange()
                }
            }
        }

        setGenreSpinner()
        setPeriodicitySpinner()
    }

    private fun bindDataToViews() {

        val requestOptions = RequestOptions().skipMemoryCache(true)

        Glide.with(this)
            .load(mBook.remotePosterUri)
            .apply(requestOptions)
            .into(mPosterImageView)

        Glide.with(this)
            .load(mBook.remoteCoverUri)
            .apply(requestOptions)
            .into(mCoverImageView)

        mTitleTextView.text = mBook.title
        mSynopsisTextView.text = mBook.synopsis
        mReadingsTextView.text = getString(R.string.simple_number_integer, mBook.readingNumber)
        mRatingTextView.text = getString(R.string.simple_number_float, mBook.rating)
        mIncomeTextView.text = getString(R.string.income_amount, mBook.income)
        mGenreSpinner.setSelection(mBook.convertGenreToPosition())
        mPeriodicitySpinner.setSelection(mBook.convertPeriodicityToPosition())
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE -> {
                val result = CropImage.getActivityResult(data)

                if (resultCode == Activity.RESULT_OK) {
                    onImageReady(result.uri)

                } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                    onError()
                }
            }

            PDF_REQUEST_CODE -> {
                /*val filesUris = arrayListOf<Uri>()

                if (resultCode == Activity.RESULT_OK && data != null) {

                    if (mBookView.isLaunchingFullBook()) {
                        mBookView.onFullBookPDFFileReady(data.data)
                    } else {
                        //Has selected several chapters
                        if (data.clipData != null) {

                            (0 until (data.clipData.itemCount)).mapTo(filesUris) { index ->
                                data.clipData.getItemAt(index).uri
                            }
                            mBookView.onSeriesChaptersPDFsFilesReady(filesUris)
                            //Has selected one chapter
                        } else {
                            mBookView.onSeriesChaptersPDFsFilesReady(arrayListOf(data.data))
                        }
                    }

                } else {
                    mBookView.onError(Exception(getString(R.string.resource_error)))
                }*/
            }
        }
    }

    private fun onImageReady(pictureUri: Uri) {
        when (mImageDestination) {
            ImageDestination.COVER -> {
                mBook.localCoverUri = pictureUri.toString()
                placeAndUploadCoverImage()
            }
            ImageDestination.POSTER -> {
                mBook.localPosterUri = pictureUri.toString()
                placeAndUploadPosterImage()
            }
        }
    }

    private fun placeAndUploadCoverImage() {
        mBook.localCoverUri?.let {
            val requestOptions =
                RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE).skipMemoryCache(true)
            val transitionOptions = DrawableTransitionOptions.withCrossFade()

            Glide.with(context!!)
                .load(Uri.parse(mBook.localCoverUri))
                .apply(requestOptions)
                .transition(transitionOptions)
                .into(mCoverImageView)

            launch(CommonPool) {
                (activity as? MyCreationsPresenterActivity)?.updateBookCover(mBook)?.consumeEach { progress ->
                    progress?.let {
                        launch(UI) {
                            if (progress == UPLOAD_STATUS_FAIL)
                                Toasty.error(activity!!, getString(R.string.cover_update_fail)).show()
                            else
                                Toasty.success(activity!!, getString(R.string.cover_updated)).show()
                        }
                    }
                }
            }

        }
    }

    private fun placeAndUploadPosterImage() {
        mBook.localPosterUri?.let {
            val requestOptions =
                RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE).skipMemoryCache(true)
            val transitionOptions = DrawableTransitionOptions.withCrossFade()

            Glide.with(context!!)
                .load(Uri.parse(mBook.localPosterUri))
                .apply(requestOptions)
                .transition(transitionOptions)
                .into(mPosterImageView)

            launch(CommonPool) {
                (activity as? MyCreationsPresenterActivity)?.updateBookPoster(mBook)?.consumeEach { progress ->
                    progress?.let {
                        launch(UI) {
                            if (progress == UPLOAD_STATUS_FAIL)
                                Toasty.error(activity!!, getString(R.string.poster_update_fail)).show()
                            else
                                Toasty.success(activity!!, getString(R.string.poster_updated)).show()
                        }
                    }
                }
            }
        }
    }

    private fun onError() {
        Toasty.error(context!!, getString(R.string.resource_error)).show()
    }

    override fun notifyTitleChange(title: String) {
        mTitleTextView.text = title
        notifySimpleChange()
    }

    override fun notifySynopsisChange(synopsis: String) {
        mSynopsisTextView.text = synopsis
        notifySimpleChange()
    }

    private fun notifySimpleChange() {
        (activity as? MyCreationsPresenterActivity)?.notifyBookEditionToDatabase(mBook)
    }

    override fun refreshFragmentData(book: Book) {
        mBook = book

        bindDataToViews()
    }
}