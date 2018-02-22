package com.andrehaueisen.cronicalia.utils.extensions


import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity

/**
 * Created by andre on 2/19/2018.
 */
fun AppCompatActivity.addFragment(containerId: Int, fragment: Fragment){

    this.supportFragmentManager.beginTransaction().add(containerId, fragment).commit()
}

fun AppCompatActivity.replaceFragment(containerId: Int, fragment: Fragment){
    this.supportFragmentManager.beginTransaction().replace(containerId, fragment).commit()
}