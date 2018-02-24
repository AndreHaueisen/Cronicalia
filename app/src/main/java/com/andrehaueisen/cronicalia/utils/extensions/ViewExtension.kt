package com.andrehaueisen.cronicalia.utils.extensions

import android.support.design.widget.Snackbar
import android.view.View

/**
 * Created by andre on 2/23/2018.
 */

fun View.showSnackbar(message: String, duration: Int = Snackbar.LENGTH_SHORT){
    Snackbar.make(this, message, duration).show()
}