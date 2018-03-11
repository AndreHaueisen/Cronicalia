package com.andrehaueisen.cronicalia.d_create_book.mvp

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import com.andrehaueisen.cronicalia.PDF_REQUEST_CODE
import com.andrehaueisen.cronicalia.R
import com.andrehaueisen.cronicalia.a_application.BaseApplication
import com.andrehaueisen.cronicalia.d_create_book.dagger.CreateBookModule
import com.andrehaueisen.cronicalia.d_create_book.dagger.DaggerCreateBookComponent
import com.andrehaueisen.cronicalia.models.Book
import com.theartofdev.edmodo.cropper.CropImage
import kotlinx.coroutines.experimental.channels.SubscriptionReceiveChannel
import javax.inject.Inject


class CreateBookActivity : AppCompatActivity() {

    private lateinit var mBookView: CreateBookView

    @Inject
    lateinit var mModel : CreateBookModel

    interface BookResources {
        fun onImageReady(pictureUri: Uri)
        fun onFullBookPDFFileReady(fileUri: Uri)
        fun onSeriesChaptersPDFsFilesReady(filesUris: ArrayList<Uri>)
        fun onError(exception: Exception)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        DaggerCreateBookComponent.builder()
            .applicationComponent(BaseApplication.get(this).getAppComponent())
            .createBookModule(CreateBookModule())
            .build()
            .inject(this)

        setSupportActionBar(findViewById<Toolbar>(R.id.create_book_app_bar))
        actionBar?.let {
            title = getString(R.string.create_new_book)
        }

        setContentView(R.layout.d_activity_create_book)

        mBookView = CreateBookView(this, mModel.getUser().getUserBookNumber(),  mModel.getUser().name,  mModel.getUser().encodedEmail, savedInstanceState)
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

    suspend fun uploadBookFiles(book: Book): SubscriptionReceiveChannel<Double?>{
        return mModel.uploadBookFiles(book)
    }
}
