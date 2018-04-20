package com.andrehaueisen.cronicalia.i_login

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.andrehaueisen.cronicalia.LOGIN_REQUEST_CODE
import com.andrehaueisen.cronicalia.R
import com.andrehaueisen.cronicalia.b_firebase.Authenticator
import com.andrehaueisen.cronicalia.b_firebase.DataRepository
import com.andrehaueisen.cronicalia.c_featured_books.mvp.FeaturedBooksPresenterActivity
import com.andrehaueisen.cronicalia.models.User
import com.andrehaueisen.cronicalia.utils.extensions.startNewActivity
import com.firebase.ui.auth.ErrorCodes
import com.firebase.ui.auth.IdpResponse
import es.dmoral.toasty.Toasty
import org.koin.android.ext.android.inject

class LoginActivity : AppCompatActivity() {

    private val mAuthenticator: Authenticator by inject()
    private val mDatabaseInstance: DataRepository by inject()

    val mUser: User by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (mAuthenticator.isUserLoggedIn()) {
            startCallingActivity()

        } else {
            mAuthenticator.startLoginFlow(this)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (requestCode == LOGIN_REQUEST_CODE) {
            val idpResponse = IdpResponse.fromResultIntent(data)

            if (idpResponse != null) {

                if (resultCode == Activity.RESULT_OK) {
                    mDatabaseInstance.loadLoggingInUser(mAuthenticator.getUserName()!!, mAuthenticator.getUserEncodedEmail()!!, this)
                    mAuthenticator.listenToUserChanges()
                    mAuthenticator.saveUserIdOnLogin()
                } else {

                    when (idpResponse.error?.errorCode) {

                        ErrorCodes.NO_NETWORK -> {
                            Toasty.error(this, getString(R.string.no_network)).show()
                            startNewActivity(FeaturedBooksPresenterActivity::class.java)
                            finish()
                        }

                        ErrorCodes.UNKNOWN_ERROR -> {
                            Toasty.error(this, getString(R.string.unknown_error)).show()
                            startNewActivity(FeaturedBooksPresenterActivity::class.java)
                            finish()
                        }
                    }
                }
            } else {
                Toasty.info(this, getString(R.string.sign_in_cancelled)).show()
            }
        }

    }

    fun startCallingActivity() {

        /*if(intent.hasExtra(INTENT_CALLING_ACTIVITY)){
            val politicianName : String? = intent.extras.getString(INTENT_POLITICIAN_NAME)
            val callingActivity: String = intent.extras.getString(INTENT_CALLING_ACTIVITY)

            when(callingActivity){
                CallingActivity.MAIN_LISTS_CHOICES_PRESENTER_ACTIVITY.name -> {
                    startNewActivity(MainListsChoicesPresenterActivity::class.java)
                }

                CallingActivity.MAIN_LISTS_PRESENTER_ACTIVITY.name -> {}

                CallingActivity.POLITICIAN_SELECTOR_PRESENTER_ACTIVITY.name -> {
                    val extras = Bundle()
                    if (politicianName != null){
                        extras.putString(INTENT_POLITICIAN_NAME, politicianName)
                    }
                    startNewActivity(PoliticianSelectorPresenterActivity::class.java, extras = extras)
                }

                CallingActivity.USER_VOTE_LIST_PRESENTER_ACTIVITY.name ->{
                    startNewActivity(UserVoteListPresenterActivity::class.java)
                }
            }
            }*/
        startNewActivity(FeaturedBooksPresenterActivity::class.java)
        finish()

    }
}
