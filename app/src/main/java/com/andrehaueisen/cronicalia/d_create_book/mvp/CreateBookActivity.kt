package com.andrehaueisen.cronicalia.d_create_book.mvp

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import com.andrehaueisen.cronicalia.PDF_REQUEST_CODE
import com.andrehaueisen.cronicalia.R
import com.andrehaueisen.cronicalia.models.Book
import com.andrehaueisen.cronicalia.models.User
import com.theartofdev.edmodo.cropper.CropImage


class CreateBookActivity : AppCompatActivity() {

    //inject User
    private val mUser = User()

    val fakeBooks = arrayListOf<Book>(Book(
        "The good fella",
        Book.BookGenre.COMEDY,
        5.5F,
        10,
        250F,
        10000,
        Book.BookLanguage.ENGLISH), Book(
        "When the sun hit the flor",
        Book.BookGenre.FICTION,
        9.5F,
        1500,
        500.50F,
        5700000,
        Book.BookLanguage.ENGLISH))


    private lateinit var mBookView: CreateBookView

    interface BookResources {
        fun onImageReady(pictureUri: Uri)
        fun onFullBookPDFFileReady(fileUri: Uri)
        fun onSeriesChaptersPDFsFilesReady(filesUris: ArrayList<Uri>)
        fun onError(exception: Exception)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setSupportActionBar(findViewById<Toolbar>(R.id.create_book_app_bar))
        actionBar?.let {
            title = getString(R.string.create_new_book)
        }

        setContentView(R.layout.d_activity_create_book)

        mBookView = CreateBookView(this, /*mUser.getUserBookNumber()*/2, fakeBooks[0])
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

                    if (data.clipData != null) {

                        (0 until(data.clipData.itemCount)).mapTo(filesUris) { index ->
                            data.clipData.getItemAt(index).uri
                        }

                        mBookView.onSeriesChaptersPDFsFilesReady(filesUris)

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
