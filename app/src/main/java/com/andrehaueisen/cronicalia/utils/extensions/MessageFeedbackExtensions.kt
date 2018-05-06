package com.andrehaueisen.cronicalia.utils.extensions

import android.app.Activity
import com.andrehaueisen.cronicalia.R
import com.andrehaueisen.cronicalia.UPLOAD_STATUS_FAIL
import com.andrehaueisen.cronicalia.UPLOAD_STATUS_OK
import com.google.firebase.storage.StorageException

fun Int.showRequestFeedback(
    activity: Activity,
    positiveMessageId: Int,
    negativeMessageId: Int,
    shouldFinishActivity: Boolean = false
) {

    when (this) {

        UPLOAD_STATUS_OK ->
            activity.showSuccessMessage(positiveMessageId, shouldFinishActivity = shouldFinishActivity)

        UPLOAD_STATUS_FAIL ->
            activity.showErrorMessage(negativeMessageId)

        StorageException.ERROR_UNKNOWN ->
            activity.showErrorMessage(R.string.check_internet_connection)

        else -> return
    }
}