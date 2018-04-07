package com.andrehaueisen.cronicalia.c_creations.mvp

import android.os.Bundle
import android.support.v4.app.FragmentActivity
import android.support.v7.app.AlertDialog
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import com.andrehaueisen.cronicalia.R
import com.andrehaueisen.cronicalia.UPLOAD_STATUS_FAIL
import com.andrehaueisen.cronicalia.UPLOAD_STATUS_OK
import com.andrehaueisen.cronicalia.d_create_book.mvp.CreateBookView
import com.google.firebase.storage.StorageException
import es.dmoral.toasty.Toasty

class UploadProgressDialog(private val fragmentActivity: FragmentActivity, private val isSmallScreen: Boolean): AlertDialog(fragmentActivity), CreateBookView.UploadState {

    private lateinit var mTitleTextView: TextView
    private lateinit var mLoadingProgressBar: ProgressBar

    //TODO put animatedVectorDrawable in the dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.d_dialog_upload_progress)
        setCancelable(false)

        mTitleTextView = findViewById(R.id.dialog_title_text_view)!!
        mTitleTextView.text = context.getString(R.string.progress_updating_book)
        mLoadingProgressBar = findViewById(R.id.loading_view)!!
        mLoadingProgressBar.progress = 0

        findViewById<Button>(R.id.background_button)!!.setOnClickListener {
            dismiss()
        }
    }

    override fun onUploadStateChanged(progress: Int) {

        when (progress) {
            UPLOAD_STATUS_OK -> {

                mLoadingProgressBar.progress = progress
                mTitleTextView.text = context.getString(R.string.progress_dialog_success)
                Toasty.success(context, context.getString(R.string.book_created))
                dismiss()
                if(isSmallScreen) {
                    //val fragment = fragmentActivity.supportFragmentManager.findFragmentByTag(FRAGMENT_EDIT_CREATION_TAG)
                    fragmentActivity.supportFragmentManager.popBackStackImmediate()
                }
            }
            UPLOAD_STATUS_FAIL -> {
                Toasty.error(context, context.getString(R.string.progress_dialog_fail)).show()
                dismiss()
            }
            StorageException.ERROR_UNKNOWN ->{
                Toasty.error(context, context.getString(R.string.check_internet_connection)).show()
                dismiss()
            }

            else  -> {
                mLoadingProgressBar.progress = progress
            }
        }
    }
}