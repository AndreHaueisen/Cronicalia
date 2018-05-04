package com.andrehaueisen.cronicalia.f_my_books.mvp

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.Fragment
import android.support.v4.content.res.ResourcesCompat
import android.support.v4.util.ArraySet
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.andrehaueisen.cronicalia.*
import com.andrehaueisen.cronicalia.f_my_books.DeleteBookAlertDialog
import com.andrehaueisen.cronicalia.f_my_books.EditTextDialog
import com.andrehaueisen.cronicalia.f_my_books.EditionFilesAdapter
import com.andrehaueisen.cronicalia.f_my_books.UploadProgressDialog
import com.andrehaueisen.cronicalia.models.Book
import com.andrehaueisen.cronicalia.utils.extensions.createUserDirectory
import com.andrehaueisen.cronicalia.utils.extensions.getSmallestScreenWidth
import com.andrehaueisen.cronicalia.utils.extensions.showRequestFeedback
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import es.dmoral.toasty.Toasty
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.channels.SubscriptionReceiveChannel
import kotlinx.coroutines.experimental.channels.consumeEach
import kotlinx.coroutines.experimental.launch
import java.io.File

/**
 * Created by andre on 3/19/2018.
 */
class MyBookEditViewFragment : Fragment(), MyBooksPresenterActivity.MyBooksPresenterInterface, EditTextDialog.BookChangesListener,
    DeleteBookAlertDialog.DeleteBookListener {

    private lateinit var mBookIsolated: Book
    private lateinit var mImageDestination: ImageDestination
    private lateinit var mPosterImageView: ImageView
    private lateinit var mCoverImageView: ImageView
    private lateinit var mChangePosterButton: Button
    private lateinit var mChangeCoverButton: Button
    private lateinit var mTitleTextView: TextView
    private lateinit var mSynopsisTextView: TextView
    private lateinit var mReadingsTextView: TextView
    private lateinit var mRatingTextView: TextView
    private lateinit var mIncomeTextView: TextView
    private lateinit var mGenreSpinner: Spinner
    private lateinit var mPeriodicitySpinner: Spinner
    private lateinit var mChaptersRecyclerView: RecyclerView
    private lateinit var mAddChapterFab: FloatingActionButton
    private lateinit var mScheduleNotice: TextView
    private lateinit var mSaveFileChangesButton: Button
    private lateinit var mDeleteBookFab: Button

    private val mFileUrisToBeDeleted = ArraySet<String>()

    private enum class ImageDestination {
        COVER, POSTER
    }

    companion object {

        fun newInstance(bundle: Bundle? = null): MyBookEditViewFragment {

            val fragment = MyBookEditViewFragment()
            bundle?.let {
                fragment.arguments = bundle
            }

            return fragment
        }
    }

    interface UploadState {
        fun onUploadStateChanged(progress: Int, subscriptionChannel: SubscriptionReceiveChannel<Int>)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelable(PARCELABLE_BOOK, mBookIsolated)
        outState.putBoolean(PARCELABLE_IS_SAVE_BUTTON_SHOWING, mSaveFileChangesButton.visibility == View.VISIBLE)
        outState.putStringArray(PARCELABLE_FILES_TO_BE_DELETED, mFileUrisToBeDeleted.toTypedArray())
        super.onSaveInstanceState(outState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.f_fragment_my_book_edit, container, false)

        mPosterImageView = view.findViewById(R.id.poster_image_view)
        mCoverImageView = view.findViewById(R.id.cover_image_view)
        mChangePosterButton = view.findViewById(R.id.change_poster_button)
        mChangeCoverButton = view.findViewById(R.id.change_cover_button)
        mTitleTextView = view.findViewById(R.id.title_text_view)
        mSynopsisTextView = view.findViewById(R.id.synopsis_text_view)
        mReadingsTextView = view.findViewById(R.id.readings_text_view)
        mRatingTextView = view.findViewById(R.id.rating_text_view)
        mIncomeTextView = view.findViewById(R.id.income_text_view)
        mGenreSpinner = view.findViewById(R.id.genre_spinner)
        mPeriodicitySpinner = view.findViewById(R.id.periodicity_spinner)
        mChaptersRecyclerView = view.findViewById(R.id.chapters_recycler_view)
        mAddChapterFab = view.findViewById(R.id.add_chapter_fab)
        mScheduleNotice = view.findViewById(R.id.schedule_status_text_view)
        mSaveFileChangesButton = view.findViewById(R.id.save_file_changes_button)
        mDeleteBookFab = view.findViewById(R.id.delete_book_fab)

        savedInstanceState?.let {
            mBookIsolated = savedInstanceState.getParcelable(PARCELABLE_BOOK)
            mFileUrisToBeDeleted.addAll(savedInstanceState.getStringArray(PARCELABLE_FILES_TO_BE_DELETED))

            if (savedInstanceState.getBoolean(PARCELABLE_IS_SAVE_BUTTON_SHOWING))
                mSaveFileChangesButton.visibility = View.VISIBLE
            else
                mSaveFileChangesButton.visibility = View.INVISIBLE
        }

        if (!mBookIsolated.isLaunchedComplete) {
            mGenreSpinner.visibility = View.VISIBLE
            mPeriodicitySpinner.visibility = View.VISIBLE
            mAddChapterFab.visibility = View.VISIBLE
        }

        initiateTitleTextView()
        initiateSynopsisTextView()
        initiateSpinners()
        initiateRecyclerView()
        initiateButtons()
        initiateScheduleTextView()
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

    private fun initiateRecyclerView() {
        mChaptersRecyclerView.layoutManager = LinearLayoutManager(context!!)
        mChaptersRecyclerView.setHasFixedSize(true)
        mChaptersRecyclerView.adapter = EditionFilesAdapter(this, mChaptersRecyclerView, mBookIsolated, mFileUrisToBeDeleted)
    }

    private fun initiateButtons() {
        mChangeCoverButton.setOnClickListener {
            val file: File
            val directoryName: String

            if (mBookIsolated.localCoverUri != null) {
                directoryName = mBookIsolated.localCoverUri!!.substringBeforeLast("/").substringAfterLast("/")
                file = requireActivity()
                    .cacheDir
                    .createUserDirectory(directoryName, FILE_NAME_BOOK_COVER)
            } else {
                file = requireContext().cacheDir.createUserDirectory("book_0${mBookIsolated.bookPosition}", FILE_NAME_BOOK_COVER)
            }

            CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setActivityTitle(context!!.getString(R.string.cover))
                .setOutputUri(Uri.fromFile(file))
                .setAspectRatio(3, 4)
                .start(requireContext(), this)

            mImageDestination = ImageDestination.COVER

        }

        mChangePosterButton.setOnClickListener {

            val file: File
            val directoryName: String

            if (mBookIsolated.localPosterUri != null) {
                directoryName = mBookIsolated.localPosterUri!!.substringBeforeLast("/").substringAfterLast("/")
                file = requireActivity()
                    .cacheDir
                    .createUserDirectory(directoryName, FILE_NAME_BOOK_POSTER)
            } else {
                file = requireContext().cacheDir.createUserDirectory("book_0${mBookIsolated.bookPosition}", FILE_NAME_BOOK_POSTER)
            }

            CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setActivityTitle(context!!.getString(R.string.poster))
                .setAspectRatio(16, 9)
                .setOutputUri(Uri.fromFile(file))
                .start(requireContext(), this)

            mImageDestination = ImageDestination.POSTER

        }


        if (mBookIsolated.isLaunchedComplete) {

            mAddChapterFab.visibility = View.GONE

        } else {

            mAddChapterFab.setOnClickListener {
                val allowMultipleFiles = false

                val intent = Intent()
                intent.type = "application/pdf"
                intent.action = Intent.ACTION_GET_CONTENT
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, allowMultipleFiles)

                startActivityForResult(Intent.createChooser(intent, "Select Pdf"), PDF_ADD_CODE, null)
            }

        }

        mSaveFileChangesButton.setOnClickListener {
            if (isChangeOnlyInFileOrder()) {
                async(UI) {
                    val result = (requireActivity() as MyBooksPresenterActivity).updateBookPdfsReferences(mBookIsolated)
                    result.showRequestFeedback(requireContext(), R.string.book_updated, R.string.update_fail)
                }

            } else {
                val uploadDialog = UploadProgressDialog(
                    requireActivity(),
                    requireActivity().getSmallestScreenWidth() < 600
                )
                uploadDialog.show()

                launch(UI) {
                    val receiveChannel = (requireActivity() as MyBooksPresenterActivity).updateBookPdfs(mBookIsolated, mFileUrisToBeDeleted)
                    receiveChannel.consumeEach { progress ->
                        progress.let {
                            launch(UI) {
                                uploadDialog.onUploadStateChanged(it, receiveChannel)
                                if (it == UPLOAD_STATUS_OK)
                                    mSaveFileChangesButton.visibility = View.INVISIBLE
                            }
                        }
                    }
                }
            }
        }

        mDeleteBookFab.setOnClickListener {
            val deleteBookDialog = DeleteBookAlertDialog()
            deleteBookDialog.setTargetFragment(this, 0)
            deleteBookDialog.show(fragmentManager, DIALOG_DELETE_BOOK_TAG)
        }
    }

    private fun initiateScheduleTextView() {

        fun setTextViewDrawable(textView: TextView, color: Int) {

            val textDrawable = resources.getDrawable(R.drawable.ic_arrow_upward_24dp, null)
            textDrawable.setTint(color)
            textView.setCompoundDrawablesRelativeWithIntrinsicBounds(null, textDrawable, null, null)
        }

        if (!mBookIsolated.isCurrentlyComplete) {

            mScheduleNotice.visibility = View.VISIBLE

            val timeRemainingOnSchedule = mBookIsolated.getTimeRemainingOnChapterScheduleInDays()!!

            if (timeRemainingOnSchedule > 0) {
                val discreteColor = ResourcesCompat.getColor(resources, R.color.text_tertiary, null)
                mScheduleNotice.text =
                        resources.getQuantityString(R.plurals.on_schedule_notice, timeRemainingOnSchedule, timeRemainingOnSchedule)
                mScheduleNotice.setTextColor(discreteColor)

            } else {
                val accentColor = ResourcesCompat.getColor(resources, R.color.colorAccent, null)
                mScheduleNotice.text = getString(R.string.behind_on_schedule_notice)
                mScheduleNotice.setTextColor(accentColor)
                setTextViewDrawable(mScheduleNotice, accentColor)

            }

        } else {
            mScheduleNotice.visibility = View.GONE
        }
    }

    override fun onDeletionConfirmed() {
        if (activity != null && isAdded) {
            (requireActivity() as MyBooksPresenterActivity).deleteBook(mBookIsolated)
            Toasty.success(requireContext(), getString(R.string.book_deleted)).show()
            fragmentManager?.popBackStackImmediate()
        }
    }

    private fun isChangeOnlyInFileOrder(): Boolean {
        if (mFileUrisToBeDeleted.isNotEmpty())
            return false

        mBookIsolated.remoteChapterUris.forEach { uri ->
            if (!uri.startsWith("https://firebasestorage"))
                return false
        }

        return true
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
        mReadingsTextView.text = getString(R.string.simple_number_integer, mBookIsolated.readingsNumber)
        mRatingTextView.text = getString(R.string.simple_number_float, mBookIsolated.rating)
        mIncomeTextView.text = getString(R.string.income_amount, mBookIsolated.income)
        mGenreSpinner.setSelection(mBookIsolated.convertGenreToPosition())
        mPeriodicitySpinner.setSelection(mBookIsolated.convertPeriodicityToPosition())
    }

    private fun setSpinnersListener() {

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
                        notifySimpleChange(mBookIsolated.genre.toString(), MyBooksModel.SimpleUpdateVariable.GENRE)
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

                initiateScheduleTextView()
                notifySimpleChange(mBookIsolated.periodicity.toString(), MyBooksModel.SimpleUpdateVariable.PERIODICITY)
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

                if (resultCode == Activity.RESULT_OK && data != null) {
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

            launch(UI) {
                if (activity != null && isAdded) {
                    val result = (activity as MyBooksPresenterActivity).updateBookCover(mBookIsolated)
                    result.showRequestFeedback(activity!!, R.string.cover_updated, R.string.cover_update_fail)
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

            launch(UI) {
                if (activity != null && isAdded) {
                    val result = (activity as MyBooksPresenterActivity).updateBookPoster(mBookIsolated)
                    result.showRequestFeedback(activity!!, R.string.poster_updated, R.string.poster_update_fail)
                }
            }
        }
    }

    private fun onError() {
        if (activity != null && isAdded)
            Toasty.error(requireContext(), getString(R.string.resource_error)).show()
    }

    fun notifyChangeOnFileDetected() {
        mSaveFileChangesButton.visibility = View.VISIBLE
    }

    override fun notifyTitleChange(title: String) {
        mTitleTextView.text = title
        notifySimpleChange(title, MyBooksModel.SimpleUpdateVariable.TITLE)
    }

    override fun notifySynopsisChange(synopsis: String) {
        mSynopsisTextView.text = synopsis
        notifySimpleChange(synopsis, MyBooksModel.SimpleUpdateVariable.SYNOPSIS)
    }

    private fun notifySimpleChange(newValue: String, variableToUpdate: MyBooksModel.SimpleUpdateVariable) {

        val collectionLocation = mBookIsolated.getDatabaseCollectionLocation()
        val bookKey = mBookIsolated.generateBookKey()

        (activity as? MyBooksPresenterActivity)?.notifySimpleBookEdition(newValue, collectionLocation, bookKey, variableToUpdate)
    }

    override fun initializeFragmentData(book: Book) {
        mBookIsolated = book
    }

    override fun refreshFragmentData(book: Book) {
        mBookIsolated = book

        bindDataToViews()
    }
}