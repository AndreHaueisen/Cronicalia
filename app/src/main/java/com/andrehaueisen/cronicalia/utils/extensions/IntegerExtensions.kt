package com.andrehaueisen.cronicalia.utils.extensions

import android.content.Context
import com.andrehaueisen.cronicalia.R
import com.andrehaueisen.cronicalia.UPLOAD_STATUS_FAIL
import com.andrehaueisen.cronicalia.UPLOAD_STATUS_OK
import com.google.firebase.storage.StorageException
import es.dmoral.toasty.Toasty

fun Int.showRequestFeedback(context: Context, positiveMessageId: Int, negativeMessageId: Int){

    val positiveMessage = context.getString(positiveMessageId)
    val negativeMessage = context.getString(negativeMessageId)

    when(this){
        UPLOAD_STATUS_OK -> Toasty.success(context, positiveMessage).show()
        UPLOAD_STATUS_FAIL -> Toasty.error(context, negativeMessage).show()
        StorageException.ERROR_UNKNOWN -> Toasty.error(context, context.getString(R.string.check_internet_connection)).show()
        else -> return
    }
}