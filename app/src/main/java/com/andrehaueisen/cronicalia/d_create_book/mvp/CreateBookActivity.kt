package com.andrehaueisen.cronicalia.d_create_book.mvp

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import com.andrehaueisen.cronicalia.PDF_REQUEST_CODE
import com.andrehaueisen.cronicalia.R
import com.andrehaueisen.cronicalia.models.User
import com.theartofdev.edmodo.cropper.CropImage


class CreateBookActivity : AppCompatActivity() {

    //inject User
    private val mUser = User()

    private lateinit var mBookView: CreateBookView

    interface BookResources {
        fun onImageReady(pictureUri: Uri)
        fun onFullBookPDFFileReady(filesUri: Uri)
        fun onChaptersPDFsFilesReady(filesUris: ArrayList<Uri>)
        fun onError(exception: Exception)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setSupportActionBar(findViewById<Toolbar>(R.id.create_book_app_bar))
        actionBar?.let {
            title = getString(R.string.create_new_book)
        }

        setContentView(R.layout.d_activity_create_book)

        mBookView = CreateBookView(this, /*mUser.getUserBookNumber()*/2)
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

                if (resultCode == Activity.RESULT_OK) {

                    if (data!!.hasExtra(Intent.EXTRA_ALLOW_MULTIPLE)) {
                        for (i in 0..data.clipData.itemCount) {
                            filesUris[i] = data.clipData.getItemAt(i).uri
                        }
                        mBookView.onChaptersPDFsFilesReady(filesUris)

                    } else {
                        mBookView.onFullBookPDFFileReady(data.data)
                    }

                } else {
                    mBookView.onError(Exception(getString(R.string.resource_error)))
                }
            }
        }
    }
}
