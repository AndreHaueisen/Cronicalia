package com.andrehaueisen.cronicalia.a_application

import android.util.Log
import com.andrehaueisen.cronicalia.SHARED_MESSAGE_TOKEN
import com.andrehaueisen.cronicalia.utils.extensions.putValueOnSharedPreferences
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.FirebaseInstanceIdService

/**
 * Created by andre on 7/5/2017.
 */
class InstanceIdService : FirebaseInstanceIdService() {

    private val LOG_TAG = InstanceIdService::class.java.simpleName

    override fun onTokenRefresh() {
        val refreshedToken = FirebaseInstanceId.getInstance().token
        Log.d(LOG_TAG, "Refreshed token: " + refreshedToken!!)

        saveTokenOnSharedPreferences(refreshedToken)
    }

    private fun saveTokenOnSharedPreferences(token: String) = applicationContext.putValueOnSharedPreferences(SHARED_MESSAGE_TOKEN, token)

}