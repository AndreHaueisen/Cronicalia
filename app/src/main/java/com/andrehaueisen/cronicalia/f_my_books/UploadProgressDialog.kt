package com.andrehaueisen.cronicalia.f_my_books

import android.os.Bundle
import android.support.v4.app.FragmentActivity
import android.support.v7.app.AlertDialog
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import com.andrehaueisen.cronicalia.R
import com.andrehaueisen.cronicalia.UPLOAD_STATUS_FAIL
import com.andrehaueisen.cronicalia.UPLOAD_STATUS_OK
import com.andrehaueisen.cronicalia.f_my_books.mvp.MyBookEditViewFragment
import com.andrehaueisen.cronicalia.utils.extensions.showRequestFeedback
import com.google.firebase.storage.StorageException
import kotlinx.coroutines.experimental.channels.SubscriptionReceiveChannel

class UploadProgressDialog(private val fragmentActivity: FragmentActivity, private val isSmallScreen: Boolean): AlertDialog(fragmentActivity), MyBookEditViewFragment.UploadState {

    private lateinit var mTitleTextView: TextView
    private lateinit var mLoadingProgressBar: ProgressBar

    //TODO put animatedVectorDrawable in the dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.f_dialog_upload_progress)
        setCancelable(false)

        mTitleTextView = findViewById(R.id.dialog_title_text_view)!!
        mTitleTextView.text = context.getString(R.string.progress_updating_book)
        mLoadingProgressBar = findViewById(R.id.loading_view)!!
        mLoadingProgressBar.progress = 0

        findViewById<Button>(R.id.background_button)!!.setOnClickListener {
            dismiss()
        }
    }

    override fun onUploadStateChanged(progress: Int, subscriptionChannel: SubscriptionReceiveChannel<Int>) {

        progress.showRequestFeedback(context, R.string.book_updated, R.string.progress_dialog_fail)

        when (progress) {
            UPLOAD_STATUS_OK -> {
                mLoadingProgressBar.progress = progress
                //TODO show confirmation animation on dialog
                subscriptionChannel.close()
                dismiss()
            }

            UPLOAD_STATUS_FAIL or StorageException.ERROR_UNKNOWN -> {
                subscriptionChannel.close()
                dismiss()
            }

            else  -> {
                mLoadingProgressBar.progress = progress
            }
        }
    }

}