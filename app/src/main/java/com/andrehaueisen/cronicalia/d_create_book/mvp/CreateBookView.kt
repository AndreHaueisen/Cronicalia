package com.andrehaueisen.cronicalia.d_create_book.mvp

import android.app.Activity
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.provider.OpenableColumns
import android.support.v4.app.ActivityCompat.startActivityForResult
import android.support.v7.widget.LinearLayoutManager
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.TextView
import com.andrehaueisen.cronicalia.*
import com.andrehaueisen.cronicalia.d_create_book.SelectedFilesAdapter
import com.andrehaueisen.cronicalia.models.Book
import com.andrehaueisen.cronicalia.utils.extensions.createBookPictureDirectory
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.d_activity_create_book.*
import kotlinx.android.synthetic.main.d_activity_create_book.view.*
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.channels.consumeEach
import kotlinx.coroutines.experimental.launch
import java.io.File


/**
 * Created by andre on 2/22/2018.
 */
class CreateBookView(
    private val mActivity: Activity,
    private val mUserBookCount: Int,
    authorName: String,
    emailId: String,
    savedState: Bundle?
) : CreateBookActivity.BookResources {

    private val LOG_TAG = CreateBookActivity::class.java.simpleName

    private enum class ImageDestination {
        COVER, POSTER
    }

    private lateinit var mImageDestination: ImageDestination
    private val mBook: Book
    private val mUploadDialog = UploadProgressDialog(mActivity)
    private val mUriTitleLinkedMap = linkedMapOf<String, String>()

    interface UploadState {
        fun onUploadStateChanged(progress: Int)
    }

    init {
        if (savedState != null) {
            mBook = savedState.getParcelable(PARCELABLE_BOOK)
            if (savedState.containsKey(PARCELABLE_URI_KEYS) && savedState.containsKey(PARCELABLE_TITLE_VALUES)) {
                restoreLinkedMap(savedState.getStringArray(PARCELABLE_URI_KEYS), savedState.getStringArray(PARCELABLE_TITLE_VALUES))
            }
            initiateChapterRecyclerView(savedState.getParcelable(PARCELABLE_LAYOUT_MANAGER))
            initiateAdapter()
            placeCoverImage()
            placePosterImage()

        } else {
            mBook = Book(authorName = authorName, authorEmailId = emailId, bookPosition = mUserBookCount)
            initiateChapterRecyclerView()
        }

        initiateBookImageViews()
        initiateTitleTextInput()
        initiateBookStatusRadioGroup()
        initiateSpinners()
        initiateFileSelectorAndUploadButton()
        initiateCancelButton()
        changeLaunchDescriptionText()
    }

    private fun initiateBookImageViews() {

        mActivity.book_cover_image_view.setOnClickListener {
            val file = mActivity
                .cacheDir
                .createBookPictureDirectory("book_0$mUserBookCount", FILE_NAME_BOOK_COVER)

            CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setActivityTitle(mActivity.getString(R.string.cover))
                .setOutputUri(Uri.fromFile(file))
                .setAspectRatio(3, 4)
                .start(mActivity)

            mImageDestination = ImageDestination.COVER
        }

        mActivity.book_poster_image_view.setOnClickListener {
            val file = mActivity
                .cacheDir
                .createBookPictureDirectory("/book_0$mUserBookCount", FILE_NAME_BOOK_POSTER)

            CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setActivityTitle(mActivity.getString(R.string.poster))
                .setAspectRatio(16, 9)
                .setOutputUri(Uri.fromFile(file))
                .start(mActivity)

            mImageDestination = ImageDestination.POSTER
        }
    }

    private fun initiateTitleTextInput() {
        mActivity.book_title_text_box.book_title_extended_edit_text.addTextChangedListener(object :
            TextWatcher {
            override fun afterTextChanged(title: Editable?) {
                mBook.title = title.toString()
                mBook.originalImmutableTitle = title.toString()
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        })

        mActivity.synopsis_title_text_box.book_synopsis_extended_edit_text.addTextChangedListener(object: TextWatcher{
            override fun afterTextChanged(synopsis: Editable?) {
                mBook.synopsis = synopsis.toString()
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        })
    }

    private fun initiateBookStatusRadioGroup() {

        with(mActivity) {
            launch_full_book_radio_button.setChecked(true)

            book_launch_status_radio_group.setOnCheckedChangeListener { _, _ ->

                if (launch_full_book_radio_button.isChecked) {
                    if (!mBook.isComplete) {
                        mBook.isComplete = true
                        mBook.periodicity = Book.ChapterPeriodicity.NONE
                        periodicity_spinner.visibility = View.GONE
                        select_files_and_upload_button.text =
                                mActivity.getString(R.string.select_book_file)

                        if (chapters_recycler_view.adapter != null) {
                            (chapters_recycler_view.adapter as SelectedFilesAdapter).clearData()
                            chapters_recycler_view.adapter = null
                        }
                    }

                } else {

                    if (mBook.isComplete) {
                        mBook.isComplete = false
                        mBook.localFullBookUri = null
                        mBook.periodicity = Book.ChapterPeriodicity.EVERY_DAY
                        periodicity_spinner.setSelection(0)
                        periodicity_spinner.visibility = View.VISIBLE
                        select_files_and_upload_button.text =
                                mActivity.getString(R.string.select_chapters_files)

                        if (chapters_recycler_view.adapter != null) {
                            (chapters_recycler_view.adapter as SelectedFilesAdapter).clearData()
                            chapters_recycler_view.adapter = null
                        }
                    }

                }


                changeLaunchDescriptionText()
            }
        }
    }

    fun isLaunchingFullBook(): Boolean {
        return mActivity.book_launch_status_radio_group.checkedRadioButtonId == R.id.launch_full_book_radio_button
    }

    private fun initiateSpinners() {

        with(mActivity) {

            fun setGenreSpinner() {
                val adapter = ArrayAdapter.createFromResource(
                    this,
                    R.array.genre_array,
                    R.layout.item_spinner
                )
                adapter.setDropDownViewResource(R.layout.item_dropdown_spinner)
                genre_spinner.adapter = adapter
                genre_spinner.onItemSelectedListener =
                        object : AdapterView.OnItemSelectedListener {
                            override fun onNothingSelected(p0: AdapterView<*>?) {
                                mBook.genre = Book.BookGenre.UNDEFINED
                            }

                            override fun onItemSelected(
                                adapter: AdapterView<*>?,
                                clickedView: View?,
                                itemPosition: Int,
                                id: Long
                            ) {

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
                                changeLaunchDescriptionText()
                            }
                        }
            }

            fun setLanguageSpinner() {
                val adapter = ArrayAdapter.createFromResource(
                    this,
                    R.array.language_array,
                    R.layout.item_spinner
                )
                adapter.setDropDownViewResource(R.layout.item_dropdown_spinner)
                language_spinner.adapter = adapter
                language_spinner.onItemSelectedListener =
                        object : AdapterView.OnItemSelectedListener {
                            override fun onNothingSelected(p0: AdapterView<*>?) {
                                mBook.language = Book.BookLanguage.UNDEFINED
                            }

                            override fun onItemSelected(
                                adapter: AdapterView<*>?,
                                clickedView: View?,
                                itemPosition: Int,
                                id: Long
                            ) {

                                when (itemPosition) {
                                    0 -> mBook.language = Book.BookLanguage.ENGLISH
                                    1 -> mBook.language = Book.BookLanguage.PORTUGUESE
                                    2 -> mBook.language = Book.BookLanguage.DEUTSCH
                                }
                                changeLaunchDescriptionText()
                            }
                        }
            }

            fun setPeriodicitySpinner() {

                val adapter = ArrayAdapter.createFromResource(
                    this,
                    R.array.periodicity_array,
                    R.layout.item_spinner
                )
                adapter.setDropDownViewResource(R.layout.item_dropdown_spinner)
                periodicity_spinner.adapter = adapter

                periodicity_spinner.onItemSelectedListener =
                        object : AdapterView.OnItemSelectedListener {
                            override fun onNothingSelected(adapter: AdapterView<*>?) {
                                mBook.periodicity = Book.ChapterPeriodicity.NONE
                            }

                            override fun onItemSelected(
                                adapter: AdapterView<*>?,
                                clickedView: View?,
                                itemPosition: Int,
                                id: Long
                            ) {
                                when (itemPosition) {
                                    0 -> mBook.periodicity = Book.ChapterPeriodicity.EVERY_DAY
                                    1 -> mBook.periodicity = Book.ChapterPeriodicity.EVERY_3_DAYS
                                    2 -> mBook.periodicity = Book.ChapterPeriodicity.EVERY_7_DAYS
                                    3 -> mBook.periodicity = Book.ChapterPeriodicity.EVERY_14_DAYS
                                    4 -> mBook.periodicity = Book.ChapterPeriodicity.EVERY_30_DAYS
                                    5 -> mBook.periodicity = Book.ChapterPeriodicity.EVERY_42_DAYS
                                }
                                changeLaunchDescriptionText()
                            }
                        }
            }

            setGenreSpinner()
            setLanguageSpinner()
            setPeriodicitySpinner()
        }
    }

    private fun changeLaunchDescriptionText() {

        with(mActivity) {
            val launchDescriptionArray =
                resources.getStringArray(R.array.launch_description_array)

            val selectedGenreView =
                genre_spinner.selectedView?.findViewById<TextView>(android.R.id.text1)
            val selectedLanguageView =
                language_spinner.selectedView?.findViewById<TextView>(android.R.id.text1)
            val genreText =
                selectedGenreView?.text?.toString()?.toLowerCase() ?: getString(R.string.action)
            val languageText = selectedLanguageView?.text?.toString()?.toLowerCase()
                    ?: getString(R.string.language)

            if (mBook.isComplete) {
                launch_description_text_view.text =
                        String.format(launchDescriptionArray[0], genreText, languageText)
            } else {
                when (mBook.periodicity) {
                    Book.ChapterPeriodicity.EVERY_DAY -> launch_description_text_view.text =
                            String.format(
                                launchDescriptionArray[1],
                                genreText,
                                languageText
                            )

                    else -> {
                        launch_description_text_view.text = String.format(
                            launchDescriptionArray[2],
                            genreText,
                            languageText,
                            mBook.periodicity.getPeriodicity()
                        )
                    }
                }
            }
        }
    }

    private fun initiateFileSelectorAndUploadButton() {

        fun uploadBookData() {
            mUploadDialog.show()

            launch(CommonPool) {
                (mActivity as CreateBookActivity).uploadBookFiles(mBook, mUriTitleLinkedMap).consumeEach { progress ->
                    progress?.let { launch(UI) { mUploadDialog.onUploadStateChanged(it) } }
                }
            }
        }

        with(mActivity) {
            select_files_and_upload_button.setOnClickListener {
                val adapter = chapters_recycler_view.adapter as? SelectedFilesAdapter

                //upload else select file
                if (adapter != null) {

                    if (adapter.areChapterTitlesValid() && mBook.isBookTitleValid(this) && mBook.isSynopsisValid(this)) {
                        uploadBookData()
                    } else
                        Toasty.error(this, getString(R.string.invalid_text_detected)).show()

                } else {

                    val allowMultipleFiles =
                        book_launch_status_radio_group.checkedRadioButtonId == R.id.launch_chapters_periodically_radio_button

                    val intent = Intent()
                    intent.type = "application/pdf"
                    intent.action = Intent.ACTION_GET_CONTENT
                    intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, allowMultipleFiles)

                    startActivityForResult(
                        this,
                        Intent.createChooser(intent, "Select Pdf"),
                        PDF_REQUEST_CODE,
                        null
                    )
                }
            }
        }
    }

    private fun initiateCancelButton() {
        with(mActivity) {
            val cancelButton = findViewById<Button>(R.id.cancel_button)
            cancelButton.setOnClickListener {
                if (chapters_recycler_view.adapter != null) {
                    (chapters_recycler_view.adapter as SelectedFilesAdapter).clearData()
                    chapters_recycler_view.adapter = null
                }
                if (book_launch_status_radio_group.checkedRadioButtonId == R.id.launch_full_book_radio_button)
                    select_files_and_upload_button.text = getString(R.string.select_book_file)
                else
                    select_files_and_upload_button.text = getString(R.string.select_chapters_files)
            }
        }
    }

    private fun initiateChapterRecyclerView(savedLayoutManagerState: Parcelable? = null) {
        mActivity.chapters_recycler_view.layoutManager = LinearLayoutManager(mActivity)
        mActivity.chapters_recycler_view.setHasFixedSize(true)
        savedLayoutManagerState?.let {
            mActivity.chapters_recycler_view.layoutManager.onRestoreInstanceState(it)
        }
    }

    fun onSaveInstanceState(bundle: Bundle): Bundle {

        if (mActivity.chapters_recycler_view.adapter != null) {
            (mActivity.chapters_recycler_view.adapter as SelectedFilesAdapter).organizeLinkedMap()

            bundle.putStringArray(PARCELABLE_URI_KEYS, getUrisFromLinkedMap())
            bundle.putStringArray(PARCELABLE_TITLE_VALUES, getTitlesFromLinkedMap())
            bundle.putParcelable(PARCELABLE_LAYOUT_MANAGER, mActivity.chapters_recycler_view.layoutManager.onSaveInstanceState())
        }

        bundle.putParcelable(PARCELABLE_BOOK, mBook)
        return bundle
    }

    override fun onImageReady(pictureUri: Uri) {

        when (mImageDestination) {
            ImageDestination.COVER -> {
                mBook.localCoverUri = pictureUri.toString()
                placeCoverImage()
            }
            ImageDestination.POSTER -> {
                mBook.localPosterUri = pictureUri.toString()
                placePosterImage()
            }
        }
    }

    private fun placeCoverImage() {
        mBook.localCoverUri?.let {
            val requestOptions =
                RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE).skipMemoryCache(true)
            val transitionOptions = DrawableTransitionOptions.withCrossFade()

            Glide.with(mActivity)
                .load(Uri.parse(mBook.localCoverUri))
                .apply(requestOptions)
                .transition(transitionOptions)
                .into(mActivity.book_cover_image_view)
        }
    }

    private fun placePosterImage() {
        mBook.localPosterUri?.let {
            val requestOptions =
                RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE).skipMemoryCache(true)
            val transitionOptions = DrawableTransitionOptions.withCrossFade()

            Glide.with(mActivity)
                .load(Uri.parse(mBook.localPosterUri))
                .apply(requestOptions)
                .transition(transitionOptions)
                .into(mActivity.book_poster_image_view)
        }
    }

    override fun onFullBookPDFFileReady(fileUri: Uri) {
        mBook.localFullBookUri = fileUri.toString()
        initiateAdapter()
    }

    override fun onSeriesChaptersPDFsFilesReady(filesUris: ArrayList<Uri>) {

        filesUris.forEach { fileUri ->
            val initialTitle = getFileTitle(fileUri)
            if (initialTitle != null) {
                mUriTitleLinkedMap[fileUri.toString()] = initialTitle.substringBefore('.')
            } else {
                mUriTitleLinkedMap[fileUri.toString()] = ""
            }
        }
        initiateAdapter()
    }

    private fun initiateAdapter() {
        if (mBook.localFullBookUri != null) {
            mActivity.chapters_recycler_view.adapter = SelectedFilesAdapter(
                mActivity,
                mActivity.chapters_recycler_view,
                mBookFileTitle = getFileTitle(Uri.parse(mBook.localFullBookUri))
            )
            mActivity.select_files_and_upload_button.text = mActivity.getString(R.string.confirm)

        } else if (mUriTitleLinkedMap.isNotEmpty()) {
            mActivity.chapters_recycler_view.adapter = SelectedFilesAdapter(
                mActivity,
                mActivity.chapters_recycler_view,
                mUriTitleLinkedMap
            )

            mActivity.select_files_and_upload_button.text = mActivity.getString(R.string.confirm)
        }
    }

    override fun onError(exception: Exception) {
        Toasty.error(mActivity, mActivity.getString(R.string.resource_error)).show()
    }

    private fun getFileTitle(uri: Uri): String? {
        val uriString = uri.toString()
        val myFile = File(uriString)

        if (uriString.startsWith("content://")) {
            var cursor: Cursor? = null

            try {
                cursor = mActivity.contentResolver.query(uri, null, null, null, null)
                if (cursor != null && cursor.moveToFirst()) {
                    return cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                }
            } finally {
                cursor?.close()
            }

        } else if (uriString.startsWith("file://")) {
            return myFile.name
        }

        return null
    }

    private fun getUrisFromLinkedMap(): Array<String>{
        return mUriTitleLinkedMap.keys.toTypedArray()
    }

    private fun getTitlesFromLinkedMap(): Array<String>{
        return mUriTitleLinkedMap.values.toTypedArray()
    }

    private fun restoreLinkedMap(keys: Array<String>, values: Array<String>){
        keys.forEachIndexed { index, key ->  mUriTitleLinkedMap[key] = values[index] }
    }

}