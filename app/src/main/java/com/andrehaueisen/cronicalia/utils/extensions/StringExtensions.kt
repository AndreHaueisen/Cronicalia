package com.andrehaueisen.cronicalia.utils.extensions

import android.content.Context
import com.andrehaueisen.cronicalia.R

/**
 * Created by andre on 2/19/2018.
 */
fun String.encodeEmail() = replace('.', ',')
fun String.decodeEmail() = replace(',', '.')


fun String.isBookTitleValid(context: Context): Boolean {
    return !this.isNullOrBlank() && this.replace(" ", "").length <= context.resources.getInteger(R.integer.title_text_box_max_length)
}


fun String.isSynopsisValid(context: Context): Boolean {
    return !this.isNullOrBlank() && this.replace(" ", "").length <= context.resources.getInteger(R.integer.synopsis_text_box_max_length)
}