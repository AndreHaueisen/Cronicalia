package com.andrehaueisen.cronicalia.utils.extensions


import android.app.Activity
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.DisplayMetrics
import com.andrehaueisen.cronicalia.R
import com.andrehaueisen.cronicalia.SHARED_PREFERENCES
import com.andrognito.flashbar.Flashbar
import com.andrognito.flashbar.anim.FlashAnim
import java.io.File



/**
 * Created by andre on 2/19/2018.
 */
fun AppCompatActivity.addFragment(containerId: Int, fragment: Fragment, tag: String? = null){

    this.supportFragmentManager.beginTransaction().add(containerId, fragment, tag).commit()
}

fun AppCompatActivity.replaceFragment(containerId: Int, fragment: Fragment, fragmentTag: String? = null, stackTag : String? = null){
    this.supportFragmentManager.beginTransaction().replace(containerId, fragment, fragmentTag).addToBackStack(stackTag).commit()
}

fun Activity.getProperLayoutManager(orientation: Int = LinearLayoutManager.VERTICAL): RecyclerView.LayoutManager {
    val smallestWidth = getSmallestScreenWidth()

    return if (smallestWidth >= 600) {
        GridLayoutManager(this, 2, orientation, false)
    } else {
        LinearLayoutManager(this, orientation, false)
    }
}

fun Activity.getSmallestScreenWidth(): Float{
    val display = this.windowManager.defaultDisplay
    val metrics = DisplayMetrics()
    display.getMetrics(metrics)

    val scaleFactor = metrics.density

    val widthDp = metrics.widthPixels / scaleFactor
    val heightDp = metrics.heightPixels / scaleFactor

    return Math.min(widthDp, heightDp)
}

fun Activity.showErrorMessage(
    negativeMessageId: Int,
    shouldFinishActivity: Boolean = false,
    barDuration: Long = Flashbar.DURATION_LONG
) {

    Flashbar.Builder(this)
        .gravity(Flashbar.Gravity.BOTTOM)
        .backgroundColorRes(R.color.colorPrimary)
        .enableSwipeToDismiss()
        .duration(barDuration)
        .title(R.string.fail)
        .message(negativeMessageId)
        .showIcon()
        .icon(R.drawable.ic_error_24dp)
        .iconColorFilterRes(android.R.color.holo_red_light)
        .iconAnimation(
            FlashAnim.with(this)
                .animateIcon()
                .alpha()
                .duration(500)
                .accelerate()
        )
        .enterAnimation(
            FlashAnim.with(this)
                .animateBar()
                .duration(750)
                .alpha()
                .overshoot()
        )
        .exitAnimation(
            FlashAnim.with(this)
                .animateBar()
                .duration(400)
                .accelerateDecelerate()
        )
        .barDismissListener(object : Flashbar.OnBarDismissListener {
            override fun onDismissing(bar: Flashbar, isSwiped: Boolean) {}

            override fun onDismissProgress(bar: Flashbar, progress: Float) {}

            override fun onDismissed(bar: Flashbar, event: Flashbar.DismissEvent) {
                if (shouldFinishActivity) this@showErrorMessage.finish()
            }
        })
        .build()
        .show()
}

fun <T : Activity> Activity.showErrorMessageAndStartActivity(
    negativeMessageId: Int,
    shouldFinishActivity: Boolean = false,
    barDuration: Long = Flashbar.DURATION_LONG,
    classToInit: Class<T>? = null
) {

    Flashbar.Builder(this)
        .gravity(Flashbar.Gravity.BOTTOM)
        .backgroundColorRes(R.color.colorPrimary)
        .enableSwipeToDismiss()
        .duration(barDuration)
        .title(R.string.fail)
        .message(negativeMessageId)
        .showIcon()
        .icon(R.drawable.ic_error_24dp)
        .iconColorFilterRes(android.R.color.holo_red_light)
        .iconAnimation(
            FlashAnim.with(this)
                .animateIcon()
                .alpha()
                .duration(500)
                .accelerate()
        )
        .enterAnimation(
            FlashAnim.with(this)
                .animateBar()
                .duration(750)
                .alpha()
                .overshoot()
        )
        .exitAnimation(
            FlashAnim.with(this)
                .animateBar()
                .duration(400)
                .accelerateDecelerate()
        )
        .barDismissListener(object : Flashbar.OnBarDismissListener {
            override fun onDismissing(bar: Flashbar, isSwiped: Boolean) {}

            override fun onDismissProgress(bar: Flashbar, progress: Float) {}

            override fun onDismissed(bar: Flashbar, event: Flashbar.DismissEvent) {
                classToInit?.let { this@showErrorMessageAndStartActivity.startNewActivity(classToInit) }

                if (shouldFinishActivity) this@showErrorMessageAndStartActivity.finish()
            }
        })
        .build()
        .show()
}

fun Activity.showSuccessMessage(
    positiveMessageId: Int,
    shouldFinishActivity: Boolean = false,
    barDuration: Long = Flashbar.DURATION_LONG
) {

    Flashbar.Builder(this)
        .gravity(Flashbar.Gravity.BOTTOM)
        .backgroundColorRes(R.color.colorPrimary)
        .enableSwipeToDismiss()
        .duration(barDuration)
        .title(R.string.success)
        .message(positiveMessageId)
        .showIcon()
        .icon(R.drawable.ic_done_24dp)
        .iconColorFilterRes(android.R.color.holo_green_dark)
        .iconAnimation(
            FlashAnim.with(this)
                .animateIcon()
                .alpha()
                .duration(500)
                .accelerate()
        )
        .enterAnimation(
            FlashAnim.with(this)
                .animateBar()
                .duration(750)
                .alpha()
                .overshoot()
        )
        .exitAnimation(
            FlashAnim.with(this)
                .animateBar()
                .duration(400)
                .accelerateDecelerate()
        )
        .barDismissListener(object : Flashbar.OnBarDismissListener {
            override fun onDismissing(bar: Flashbar, isSwiped: Boolean) {}

            override fun onDismissProgress(bar: Flashbar, progress: Float) {}

            override fun onDismissed(bar: Flashbar, event: Flashbar.DismissEvent) {
                if (shouldFinishActivity)
                    this@showSuccessMessage.finish()
            }
        })
        .build()
        .show()
}

fun Activity.showInfoMessage(
    messageId: Int,
    barDuration: Long = Flashbar.DURATION_LONG
) {

    Flashbar.Builder(this)
        .gravity(Flashbar.Gravity.BOTTOM)
        .backgroundColorRes(R.color.colorPrimary)
        .enableSwipeToDismiss()
        .duration(barDuration)
        .title(R.string.info)
        .message(messageId)
        .showIcon()
        .icon(R.drawable.ic_notice_24dp)
        .iconColorFilterRes(android.R.color.holo_blue_dark)
        .iconAnimation(
            FlashAnim.with(this)
                .animateIcon()
                .alpha()
                .duration(500)
                .accelerate()
        )
        .enterAnimation(
            FlashAnim.with(this)
                .animateBar()
                .duration(750)
                .alpha()
                .overshoot()
        )
        .exitAnimation(
            FlashAnim.with(this)
                .animateBar()
                .duration(400)
                .accelerateDecelerate()
        )
        .build()
        .show()
}

fun Activity.showProgressInfo(
    messageId: Int): Flashbar.Builder{

    return Flashbar.Builder(this)
        .gravity(Flashbar.Gravity.BOTTOM)
        .backgroundColorRes(R.color.colorPrimary)
        .enableSwipeToDismiss()
        .title(R.string.info)
        .message(messageId)
        .showIcon()
        .showProgress(Flashbar.ProgressPosition.RIGHT)
        .icon(R.drawable.ic_notice_24dp)
        .iconColorFilterRes(android.R.color.holo_blue_dark)
        .iconAnimation(
            FlashAnim.with(this)
                .animateIcon()
                .alpha()
                .duration(500)
                .accelerate()
        )
        .enterAnimation(
            FlashAnim.with(this)
                .animateBar()
                .duration(750)
                .alpha()
                .overshoot()
        )
        .exitAnimation(
            FlashAnim.with(this)
                .animateBar()
                .duration(400)
                .accelerateDecelerate()
        )

}

fun Context.isOnline(): Boolean {
    val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val netInfo = connectivityManager.activeNetworkInfo
    return netInfo != null && netInfo.isConnectedOrConnecting
}

fun <T : Activity> Context.startNewActivity(classToInit: Class<T>, flags: List<Int>? = null, extras: Bundle? = null, options: Bundle? = null) {

    val intent = Intent(this, classToInit)
    flags?.let {
        flags.forEach { flag -> intent.flags = flag }
    }

    if (extras != null) {
        intent.putExtras(extras)
    }

    if (options != null) {
        this.startActivity(intent, options)
    } else {
        this.startActivity(intent)
    }
}

inline fun <reified T : Any> Context.putValueOnSharedPreferences(key: String, data: T) {

    val editor = this.getSharedPreferences(SHARED_PREFERENCES, 0).edit()

    when (data) {
        is String -> editor.putString(key, data as String)
        is Int -> editor.putInt(key, data as Int)
        is Boolean -> editor.putBoolean(key, data as Boolean)
        is Long -> editor.putLong(key, data as Long)
        is Float -> editor.putFloat(key, data as Float)
        is Set<*> -> editor.putStringSet(key, data as Set<String>)
    }

    editor.apply()
}


/**
 * This method converts dp unit to equivalent pixels, depending on device density.
 *
 * @param dp A value in dp (density independent pixels) unit. Which we need to convert into pixels
 * @return A float value to represent px equivalent to dp depending on device density
 */
fun Context.convertDpToPixel(dp: Float): Float {
    val resources = this.resources
    val metrics = resources.displayMetrics
    return dp * (metrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
}

/**
 * This method converts device specific pixels to density independent pixels.
 *
 * @param px A value in px (pixels) unit. Which we need to convert into db
 * @return A float value to represent dp equivalent to px value
 */
fun Context.convertPixelsToDp(px: Float): Float {
    val resources = this.resources
    val metrics = resources.displayMetrics
    return px / (metrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
}

fun Context.getFileTitle(uri: Uri): String? {
    val uriString = uri.toString()
    val myFile = File(uriString)

    if (uriString.startsWith("content://")) {
        var cursor: Cursor? = null

        try {
            cursor = this.contentResolver.query(uri, null, null, null, null)
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
            }
        } finally {
            cursor?.close()
        }

    } else if (uriString.startsWith("file://")) {
        return myFile.name
    }

    return null
}

fun Context.pullStringFromSharedPreferences(key: String): String =
    this.getSharedPreferences(SHARED_PREFERENCES, 0).getString(key, null)

fun Context.pullIntFromSharedPreferences(key: String): Int =
    this.getSharedPreferences(SHARED_PREFERENCES, 0).getInt(key, 0)

fun Context.pullBooleanFromSharedPreferences(key: String, defaultValue: Boolean = false): Boolean =
    this.getSharedPreferences(SHARED_PREFERENCES, 0).getBoolean(key, defaultValue)