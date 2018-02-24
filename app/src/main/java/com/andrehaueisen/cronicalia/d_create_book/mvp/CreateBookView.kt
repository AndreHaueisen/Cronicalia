package com.andrehaueisen.cronicalia.d_create_book.mvp

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.support.design.widget.TextInputLayout
import android.support.v4.app.ActivityCompat.startActivityForResult
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import com.andrehaueisen.cronicalia.FILE_NAME_BOOK_COVER
import com.andrehaueisen.cronicalia.FILE_NAME_BOOK_POSTER
import com.andrehaueisen.cronicalia.PDF_REQUEST_CODE
import com.andrehaueisen.cronicalia.R
import com.andrehaueisen.cronicalia.models.Book
import com.andrehaueisen.cronicalia.utils.extensions.createBookPictureDirectory
import com.andrehaueisen.cronicalia.utils.extensions.showSnackbar
import com.bumptech.glide.Glide
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView


/**
 * Created by andre on 2/22/2018.
 */
class CreateBookView(private val mActivity: Activity, private val mUserBookNumber: Int) :
    CreateBookActivity.BookResources {

    private val LOG_TAG = CreateBookActivity::class.java.simpleName

    private enum class ImageDestination {
        COVER, POSTER
    }

    private val mBook = Book(title = "")
    private lateinit var mImageDestination: ImageDestination

    private val mTitleTextInput = mActivity.findViewById<TextInputLayout>(R.id.title_input_layout)
    private val mBookLaunchRadioGroup = mActivity.findViewById<RadioGroup>(R.id.book_launch_status_radio_group)
    private val mPeriodicitySpinner = mActivity.findViewById<Spinner>(R.id.periodicity_spinner)
    private val mCoverImageView = mActivity.findViewById<ImageView>(R.id.book_cover_image_view)
    private val mPosterImageView = mActivity.findViewById<ImageView>(R.id.book_poster_image_view)
    private val mFileSelectorButton = mActivity.findViewById<Button>(R.id.select_files_button)

    init {
        initiateBookImages()
        initiateTitleTextInput()
        initiateBookStatusRadioGroup()
        initiateSpinner()
        initiateFileSelectorButton()
        changeLaunchDescriptionText()
    }

    private fun initiateBookImages() {

        mCoverImageView.setOnClickListener {
            val file = mActivity
                .cacheDir
                .createBookPictureDirectory("book_0$mUserBookNumber", FILE_NAME_BOOK_COVER)

            CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setActivityTitle(mActivity.getString(R.string.cover))
                .setOutputUri(Uri.fromFile(file))
                .start(mActivity)

            mImageDestination = ImageDestination.COVER
        }

        mPosterImageView.setOnClickListener {
            val file = mActivity
                .cacheDir
                .createBookPictureDirectory("/book_0$mUserBookNumber", FILE_NAME_BOOK_POSTER)

            CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setActivityTitle(mActivity.getString(R.string.poster))
                .setOutputUri(Uri.fromFile(file))
                .start(mActivity)

            mImageDestination = ImageDestination.POSTER
        }
    }

    private fun initiateTitleTextInput() {
        mTitleTextInput.editText!!.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(title: Editable?) {
                mBook.title = title.toString()
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        })

    }

    private fun initiateBookStatusRadioGroup() {

        mBookLaunchRadioGroup.check(R.id.launch_full_book_radio_button)
        mFileSelectorButton.text = mActivity.getString(R.string.select_book_file)
        mBook.isComplete = true

        mBookLaunchRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.launch_full_book_radio_button -> {
                    mBook.isComplete = true
                    mPeriodicitySpinner.visibility = View.GONE
                    mFileSelectorButton.text = mActivity.getString(R.string.select_book_file)
                }
                R.id.launch_chapters_periodically_radio_button -> {
                    mBook.isComplete = false
                    mPeriodicitySpinner.visibility = View.VISIBLE
                    mFileSelectorButton.text = mActivity.getString(R.string.select_chapters_files)
                }
            }
            changeLaunchDescriptionText()
        }
    }

    private fun initiateSpinner() {

        val adapter = ArrayAdapter.createFromResource(
            mActivity,
            R.array.periodicity_array,
            R.layout.item_spinner
        )
        adapter.setDropDownViewResource(R.layout.item_dropdown_spinner)
        mPeriodicitySpinner.adapter = adapter

        mPeriodicitySpinner.onItemSelectedListener =
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

    private fun changeLaunchDescriptionText() {
        val launchDescriptionTextView =
            mActivity.findViewById<TextView>(R.id.launch_description_text_view)
        val launchDescriptionArray =
            mActivity.resources.getStringArray(R.array.launch_description_array)

        if (mBook.isComplete) {
            launchDescriptionTextView.text = launchDescriptionArray[0]
        } else {
            when (mBook.periodicity) {
                Book.ChapterPeriodicity.EVERY_DAY -> launchDescriptionTextView.text =
                        launchDescriptionArray[1]
                else -> {
                    launchDescriptionTextView.text = String.format(
                        launchDescriptionArray[2],
                        mBook.periodicity.getPeriodicity()
                    )
                }
            }
        }
    }

    private fun initiateFileSelectorButton(){
        mFileSelectorButton.setOnClickListener {
            val allowMultipleFiles = mBookLaunchRadioGroup.checkedRadioButtonId == R.id.launch_chapters_periodically_radio_button

            val intent = Intent()
            intent.type = "application/pdf"
            intent.action = Intent.ACTION_GET_CONTENT
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, allowMultipleFiles)

            startActivityForResult(mActivity, Intent.createChooser(intent, "Select Pdf"), PDF_REQUEST_CODE, null)
        }
    }

    override fun onImageReady(pictureUri: Uri) {
        when (mImageDestination) {
            ImageDestination.COVER -> {
                Glide.with(mActivity).load(pictureUri).into(mCoverImageView)
                mBook.localCoverUri = pictureUri.toString()
            }
            ImageDestination.POSTER -> {
                Glide.with(mActivity).load(pictureUri).into(mPosterImageView)
                mBook.localPosterUri = pictureUri.toString()
            }
        }
    }

    override fun onFullBookPDFFileReady(filesUri: Uri) {
        mBook.
    }

    override fun onChaptersPDFsFilesReady(filesUris: ArrayList<Uri>) {
        mBook.chaptersUris
    }

    override fun onError(exception: Exception) {
        with(mActivity) {
            findViewById<View>(R.id.create_book_root_view)
                .showSnackbar(getString(R.string.resource_error))
        }
    }
}