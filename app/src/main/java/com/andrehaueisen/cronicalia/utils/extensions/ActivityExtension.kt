package com.andrehaueisen.cronicalia.utils.extensions


import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.DisplayMetrics
import com.andrehaueisen.cronicalia.SHARED_PREFERENCES

/**
 * Created by andre on 2/19/2018.
 */
fun AppCompatActivity.addFragment(containerId: Int, fragment: Fragment){

    this.supportFragmentManager.beginTransaction().add(containerId, fragment).commit()
}

fun AppCompatActivity.replaceFragment(containerId: Int, fragment: Fragment, stackTag : String? = null){
    this.supportFragmentManager.beginTransaction().replace(containerId, fragment).addToBackStack(stackTag).commit()
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

fun Context.pullStringFromSharedPreferences(key: String): String =
    this.getSharedPreferences(SHARED_PREFERENCES, 0).getString(key, null)

fun Context.pullIntFromSharedPreferences(key: String): Int =
    this.getSharedPreferences(SHARED_PREFERENCES, 0).getInt(key, 0)

fun Context.pullBooleanFromSharedPreferences(key: String, defaultValue: Boolean = false): Boolean =
    this.getSharedPreferences(SHARED_PREFERENCES, 0).getBoolean(key, defaultValue)