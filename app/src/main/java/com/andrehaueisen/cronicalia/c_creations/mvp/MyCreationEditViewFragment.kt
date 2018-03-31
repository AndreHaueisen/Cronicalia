package com.andrehaueisen.cronicalia.c_creations.mvp

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.andrehaueisen.cronicalia.*
import com.andrehaueisen.cronicalia.c_creations.EditTextDialog
import com.andrehaueisen.cronicalia.c_creations.EditionFilesAdapter
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

    private lateinit var mBookIsolated: Book
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

        mBookIsolated = arguments?.getParcelable(PARCELABLE_BOOK)!!

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

        if (!mBookIsolated.isLaunchedComplete) {
            mGenreSpinner.visibility = View.VISIBLE
            mPeriodicitySpinner.visibility = View.VISIBLE
            mAddChapterFab.visibility = View.VISIBLE
        }

        initiateTitleTextView()
        initiateSynopsisTextView()
        initiateImageViews()
        initiateSpinners()
        initiateRecyclerView()
        initiateFabs()
        bindDataToViews()
        setSpinnersListener()

        return view
    }

    private fun initiateTitleTextView() {
        mTitleTextView.setOnClickListener {
            EditTextDialog(this, EditTextDialog.ViewBeingEdited.TITLE_VIEW, mBookIsolated).show()
        }
    }

    private fun initiateSynopsisTextView() {
        mSynopsisTextView.setOnClickListener {
            EditTextDialog(this, EditTextDialog.ViewBeingEdited.SYNOPSIS_VIEW, mBookIsolated).show()
        }
    }

    private fun initiateImageViews() {
        mCoverImageView.setOnClickListener {
            val file: File
            val directoryName: String

            if (mBookIsolated.localCoverUri != null) {
                directoryName = mBookIsolated.localCoverUri!!.substringBeforeLast("/").substringAfterLast("/")
                file = activity!!
                    .cacheDir
                    .createBookPictureDirectory(directoryName, FILE_NAME_BOOK_COVER)
            } else {
                file = context!!.cacheDir.createBookPictureDirectory("book_0${mBookIsolated.bookPosition}", FILE_NAME_BOOK_COVER)
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
            val directoryName = mBookIsolated.localCoverUri?.substringBeforeLast("/")?.substringAfterLast("/")

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
        }

        fun setPeriodicitySpinner() {
            val adapter = ArrayAdapter.createFromResource(context, R.array.periodicity_array, R.layout.item_spinner)
            adapter.setDropDownViewResource(R.layout.item_dropdown_spinner)
            mPeriodicitySpinner.adapter = adapter
        }

        setGenreSpinner()
        setPeriodicitySpinner()
    }

    private fun initiateRecyclerView(){
        mChaptersRecyclerView.layoutManager = LinearLayoutManager(context!!)
        mChaptersRecyclerView.setHasFixedSize(true)
        mChaptersRecyclerView.adapter = EditionFilesAdapter(this, mChaptersRecyclerView, mBookIsolated)
    }

    private fun initiateFabs(){
        if(mBookIsolated.isLaunchedComplete){

            mAddChapterFab.visibility = View.GONE
            mDeleteBookFab.visibility = View.GONE

        } else {

            mAddChapterFab.setOnClickListener {
                val allowMultipleFiles = false

                val intent = Intent()
                intent.type = "application/pdf"
                intent.action = Intent.ACTION_GET_CONTENT
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, allowMultipleFiles)

                startActivityForResult(Intent.createChooser(intent, "Select Pdf"), PDF_ADD_CODE, null)
            }

            mDeleteBookFab.setOnClickListener {  }
        }

    }

    private fun bindDataToViews() {

        val requestOptions = RequestOptions().skipMemoryCache(true)

        Glide.with(this)
            .load(mBookIsolated.remotePosterUri)
            .apply(requestOptions)
            .into(mPosterImageView)

        Glide.with(this)
            .load(mBookIsolated.remoteCoverUri)
            .apply(requestOptions)
            .into(mCoverImageView)

        mTitleTextView.text = mBookIsolated.title
        mSynopsisTextView.text = mBookIsolated.synopsis
        mReadingsTextView.text = getString(R.string.simple_number_integer, mBookIsolated.readingNumber)
        mRatingTextView.text = getString(R.string.simple_number_float, mBookIsolated.rating)
        mIncomeTextView.text = getString(R.string.income_amount, mBookIsolated.income)
        mGenreSpinner.setSelection(mBookIsolated.convertGenreToPosition())
        mPeriodicitySpinner.setSelection(mBookIsolated.convertPeriodicityToPosition())
    }

    private fun setSpinnersListener(){

        mGenreSpinner.onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {
                    override fun onNothingSelected(p0: AdapterView<*>?) {
                        mBookIsolated.genre = Book.BookGenre.UNDEFINED
                    }

                    override fun onItemSelected(adapter: AdapterView<*>?, clickedView: View?, itemPosition: Int, id: Long) {

                        when (itemPosition) {
                            0 -> mBookIsolated.genre = Book.BookGenre.ACTION
                            1 -> mBookIsolated.genre = Book.BookGenre.FICTION
                            2 -> mBookIsolated.genre = Book.BookGenre.ROMANCE
                            3 -> mBookIsolated.genre = Book.BookGenre.COMEDY
                            4 -> mBookIsolated.genre = Book.BookGenre.DRAMA
                            5 -> mBookIsolated.genre = Book.BookGenre.HORROR
                            6 -> mBookIsolated.genre = Book.BookGenre.SATIRE
                            7 -> mBookIsolated.genre = Book.BookGenre.FANTASY
                            8 -> mBookIsolated.genre = Book.BookGenre.MYTHOLOGY
                            9 -> mBookIsolated.genre = Book.BookGenre.ADVENTURE
                        }
                        notifySimpleChange(mBookIsolated.genre.toString(), MyCreationsModel.SimpleUpdateVariable.GENRE)
                    }
                }


        mPeriodicitySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(adapter: AdapterView<*>?) {
                mBookIsolated.periodicity = Book.ChapterPeriodicity.NONE
            }

            override fun onItemSelected(adapter: AdapterView<*>?, clickedView: View?, itemPosition: Int, id: Long) {
                when (itemPosition) {
                    0 -> mBookIsolated.periodicity = Book.ChapterPeriodicity.EVERY_DAY
                    1 -> mBookIsolated.periodicity = Book.ChapterPeriodicity.EVERY_3_DAYS
                    2 -> mBookIsolated.periodicity = Book.ChapterPeriodicity.EVERY_7_DAYS
                    3 -> mBookIsolated.periodicity = Book.ChapterPeriodicity.EVERY_14_DAYS
                    4 -> mBookIsolated.periodicity = Book.ChapterPeriodicity.EVERY_30_DAYS
                    5 -> mBookIsolated.periodicity = Book.ChapterPeriodicity.EVERY_42_DAYS
                }

                notifySimpleChange(mBookIsolated.periodicity.toString(), MyCreationsModel.SimpleUpdateVariable.PERIODICITY)
            }
        }
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

            PDF_EDIT_CODE -> {

                if (resultCode == Activity.RESULT_OK && data != null){
                    (mChaptersRecyclerView.adapter as EditionFilesAdapter).onEditFileReady(data.data)
                }
            }

            PDF_ADD_CODE -> {

                if (resultCode == Activity.RESULT_OK && data != null) {
                    (mChaptersRecyclerView.adapter as EditionFilesAdapter).onAddFileReady(data.data)
                }
            }
        }
    }

    private fun onImageReady(pictureUri: Uri) {
        when (mImageDestination) {
            ImageDestination.COVER -> {
                mBookIsolated.localCoverUri = pictureUri.toString()
                placeAndUploadCoverImage()
            }
            ImageDestination.POSTER -> {
                mBookIsolated.localPosterUri = pictureUri.toString()
                placeAndUploadPosterImage()
            }
        }
    }

    private fun placeAndUploadCoverImage() {
        mBookIsolated.localCoverUri?.let {
            val requestOptions =
                RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE).skipMemoryCache(true)
            val transitionOptions = DrawableTransitionOptions.withCrossFade()

            Glide.with(context!!)
                .load(Uri.parse(mBookIsolated.localCoverUri))
                .apply(requestOptions)
                .transition(transitionOptions)
                .into(mCoverImageView)

            launch(CommonPool) {
                (activity as? MyCreationsPresenterActivity)?.updateBookCover(mBookIsolated)?.consumeEach { progress ->
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
        mBookIsolated.localPosterUri?.let {
            val requestOptions =
                RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE).skipMemoryCache(true)
            val transitionOptions = DrawableTransitionOptions.withCrossFade()

            Glide.with(context!!)
                .load(Uri.parse(mBookIsolated.localPosterUri))
                .apply(requestOptions)
                .transition(transitionOptions)
                .into(mPosterImageView)

            launch(CommonPool) {
                (activity as? MyCreationsPresenterActivity)?.updateBookPoster(mBookIsolated)?.consumeEach { progress ->
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
        notifySimpleChange(title, MyCreationsModel.SimpleUpdateVariable.TITLE)
    }

    override fun notifySynopsisChange(synopsis: String) {
        mSynopsisTextView.text = synopsis
        notifySimpleChange(synopsis, MyCreationsModel.SimpleUpdateVariable.SYNOPSIS)
    }

    private fun notifySimpleChange(newValue: String, variableToUpdate: MyCreationsModel.SimpleUpdateVariable) {

        val collectionLocation = mBookIsolated.getDatabaseCollectionLocation()
        val bookKey = mBookIsolated.generateBookKey()

        (activity as? MyCreationsPresenterActivity)?.notifySimpleBookEdition(newValue, collectionLocation, bookKey, variableToUpdate)
    }

    override fun refreshFragmentData(book: Book) {
        mBookIsolated = book

        bindDataToViews()
    }
}