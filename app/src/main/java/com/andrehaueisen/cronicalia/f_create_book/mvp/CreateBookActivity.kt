package com.andrehaueisen.cronicalia.f_create_book.mvp

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import com.andrehaueisen.cronicalia.PDF_REQUEST_CODE
import com.andrehaueisen.cronicalia.R
import com.andrehaueisen.cronicalia.b_firebase.DataRepository
import com.andrehaueisen.cronicalia.b_firebase.FileRepository
import com.andrehaueisen.cronicalia.models.Book
import com.andrehaueisen.cronicalia.models.User
import com.theartofdev.edmodo.cropper.CropImage
import kotlinx.coroutines.experimental.channels.SubscriptionReceiveChannel
import org.koin.android.ext.android.inject


class CreateBookActivity : AppCompatActivity() {

    private lateinit var mBookView: CreateBookView

    private lateinit var mModel : CreateBookModel
    private val mUser : User by inject()

    interface BookResources {
        fun onImageReady(pictureUri: Uri)
        fun onFullBookPDFFileReady(fileUri: Uri)
        fun onSeriesChaptersPDFsFilesReady(filesUris: ArrayList<Uri>)
        fun onError(exception: Exception)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val fileRepository : FileRepository by inject()
        val dataRepository : DataRepository by inject()
        mModel = CreateBookModel(fileRepository, dataRepository)

        setSupportActionBar(findViewById<Toolbar>(R.id.create_book_app_bar))
        actionBar?.let {
            title = getString(R.string.create_new_book)
        }

        setContentView(R.layout.f_activity_create_book)

        mBookView = CreateBookView(this, mUser.getUserBookCount(),  mUser.name!!,  mUser.encodedEmail!!, savedInstanceState)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        when (requestCode) {
            CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE -> {
                val result = CropImage.getActivityResult(data)

                if (resultCode == Activity.RESULT_OK) {
                    mBookView.onImageReady(result.uri)

                } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                    mBookView.onError(result.error)
                }
            }

            PDF_REQUEST_CODE -> {
                val filesUris = arrayListOf<Uri>()

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
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {

        super.onSaveInstanceState(mBookView.onSaveInstanceState(outState))
    }

    suspend fun uploadBookFiles(book: Book): SubscriptionReceiveChannel<Int>{
        return mModel.uploadBookFiles(book)
    }
}
