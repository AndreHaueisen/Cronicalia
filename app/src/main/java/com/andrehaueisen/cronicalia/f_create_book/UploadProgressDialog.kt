package com.andrehaueisen.cronicalia.f_create_book

import android.app.Activity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import com.andrehaueisen.cronicalia.R
import com.andrehaueisen.cronicalia.UPLOAD_STATUS_FAIL
import com.andrehaueisen.cronicalia.UPLOAD_STATUS_OK
import com.andrehaueisen.cronicalia.f_create_book.mvp.CreateBookView
import com.andrehaueisen.cronicalia.utils.extensions.showRequestFeedback
import com.google.firebase.storage.StorageException
import kotlinx.coroutines.experimental.channels.SubscriptionReceiveChannel


/**
 * Created by andre on 11/23/2017.
 */
class UploadProgressDialog(activity: Activity): AlertDialog(activity),
    CreateBookView.UploadState {

    private val mActivity: Activity = activity
    private lateinit var mTitleTextView: TextView
    private lateinit var mLoadingProgressBar: ProgressBar

    //TODO put animatedVectorDrawable in the dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.f_dialog_upload_progress)
        setCancelable(false)

        mTitleTextView = findViewById(R.id.dialog_title_text_view)!!
        mTitleTextView.text = context.getString(R.string.progress_dialog_title)
        mLoadingProgressBar = findViewById(R.id.loading_view)!!
        mLoadingProgressBar.progress = 0

        findViewById<Button>(R.id.background_button)!!.setOnClickListener {
            this.dismiss()
        }
    }

    override fun onUploadStateChanged(progress: Int, subscriptionChannel: SubscriptionReceiveChannel<Int>) {

        progress.showRequestFeedback(context, R.string.book_created, R.string.progress_dialog_fail)

        when (progress) {
            UPLOAD_STATUS_OK -> {
                mLoadingProgressBar.progress = progress
                subscriptionChannel.close()
                this.dismiss()
                mActivity.finish()
            }
            UPLOAD_STATUS_FAIL or StorageException.ERROR_UNKNOWN -> {
                subscriptionChannel.close()
                this.dismiss()
            }

            else  -> {
                mLoadingProgressBar.progress = progress
            }
        }
    }
}