package com.andrehaueisen.cronicalia.i_login

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.andrehaueisen.cronicalia.LOGIN_REQUEST_CODE
import com.andrehaueisen.cronicalia.R
import com.andrehaueisen.cronicalia.a_application.BaseApplication
import com.andrehaueisen.cronicalia.b_firebase.Authenticator
import com.andrehaueisen.cronicalia.b_firebase.DataRepository
import com.andrehaueisen.cronicalia.c_creations.mvp.MyCreationsActivity
import com.andrehaueisen.cronicalia.models.User
import com.andrehaueisen.cronicalia.utils.extensions.startNewActivity
import com.andrehaueisen.listadejanot.j_login.dagger.DaggerLoginActivityComponent
import com.firebase.ui.auth.ErrorCodes
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ListenerRegistration
import es.dmoral.toasty.Toasty
import javax.inject.Inject

class LoginActivity : AppCompatActivity() {

    @Inject
    lateinit var mAuthenticator: Authenticator

    @Inject
    lateinit var mDatabaseInstance: DataRepository

    @Inject
    lateinit var mUser: User

    private var mUserListenerRegistration: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        DaggerLoginActivityComponent.builder()
            .applicationComponent(BaseApplication.get(this).getAppComponent())
            .build()
            .injectFirebaseAuthenticator(this)

        if (mAuthenticator.isUserLoggedIn()) {
            startCallingActivity()
            finish()

        } else {
            mAuthenticator.startLoginFlow(this)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (requestCode == LOGIN_REQUEST_CODE) {
            val idpResponse = IdpResponse.fromResultIntent(data)

            if (idpResponse != null) {

                if (resultCode == Activity.RESULT_OK) {
                    mDatabaseInstance.createUserOnServer(mAuthenticator.getUserName()!!, mAuthenticator.getUserEncodedEmail()!!)
                    mUserListenerRegistration = mDatabaseInstance.listenToUser(mUserListener, mAuthenticator.getUserEncodedEmail()!!)
                    mAuthenticator.saveUserIdOnLogin()
                } else {

                    when (idpResponse.error?.errorCode) {

                        ErrorCodes.NO_NETWORK -> {
                            Toasty.error(this, getString(R.string.no_network)).show()
                            startNewActivity(MyCreationsActivity::class.java)
                            finish()
                        }

                        ErrorCodes.UNKNOWN_ERROR -> {
                            Toasty.error(this, getString(R.string.unknown_error)).show()
                            startNewActivity(MyCreationsActivity::class.java)
                            finish()
                        }
                    }
                }
            } else {
                Toasty.info(this, getString(R.string.sign_in_cancelled)).show()
            }
        }

    }

    private val mUserListener = object : EventListener<DocumentSnapshot> {

        override fun onEvent(documentSnapshot: DocumentSnapshot?, exception: FirebaseFirestoreException?) {
            exception?.let {
                Log.e("LoginActivity", "User fetch failed")
                return
            }

            if (documentSnapshot != null && documentSnapshot.exists()) {
                val newUser = documentSnapshot.toObject(User::class.java)
                mUser.refreshUser(newUser)

                startCallingActivity()
            }
        }
    }

    private fun startCallingActivity() {

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
        mUserListenerRegistration?.remove()
        startNewActivity(MyCreationsActivity::class.java)
        finish()

    }
}
