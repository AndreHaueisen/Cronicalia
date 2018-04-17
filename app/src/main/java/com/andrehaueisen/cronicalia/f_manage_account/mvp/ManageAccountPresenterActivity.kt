package com.andrehaueisen.cronicalia.f_manage_account.mvp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.andrehaueisen.cronicalia.R
import com.andrehaueisen.cronicalia.b_firebase.DataRepository
import com.andrehaueisen.cronicalia.b_firebase.FileRepository
import com.andrehaueisen.cronicalia.c_my_books.mvp.MyBooksPresenterActivity
import com.andrehaueisen.cronicalia.e_featured_books.mvp.FeaturedBooksPresenterActivity
import com.andrehaueisen.cronicalia.models.User
import com.andrehaueisen.cronicalia.utils.extensions.startNewActivity
import com.theartofdev.edmodo.cropper.CropImage
import kotlinx.android.synthetic.main.f_activity_manage_account.*
import kotlinx.coroutines.experimental.channels.SubscriptionReceiveChannel
import org.koin.android.ext.android.inject

class ManageAccountPresenterActivity: AppCompatActivity() {

    private val mUser: User by inject()

    private lateinit var mModel: ManageAccountModel
    private lateinit var mView: ManageAccountView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.f_activity_manage_account)

        val dataRepository: DataRepository by inject()
        val fileRepository: FileRepository by inject()

        mModel = ManageAccountModel(dataRepository, fileRepository)
        mView = ManageAccountView(this, mUser)

        navigation_bottom_view.setOnNavigationItemSelectedListener { menuItem ->

            when(menuItem.itemId){
                R.id.action_featured -> {
                    startNewActivity(FeaturedBooksPresenterActivity::class.java, listOf(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT))
                }
                R.id.action_search -> {}
                R.id.action_reading_collection -> {}
                R.id.action_my_books -> {
                    startNewActivity(MyBooksPresenterActivity::class.java, listOf(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT))
                }
                R.id.action_account -> {}
            }

            false
        }

        val menuItem = navigation_bottom_view.menu.getItem(4)
        menuItem.isChecked = true
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        when (requestCode) {
            CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE -> {
                val result = CropImage.getActivityResult(data)

                if (resultCode == Activity.RESULT_OK) {
                    mView.onImageReady(result.uri)

                } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                    mView.onError()
                }
            }

        }
    }

    fun notifySimpleUpdate(textUpdate: String, variableBeingUpdated: ManageAccountModel.SimpleUserUpdateVariable){
        mModel.simpleUpdateBook(textUpdate, variableBeingUpdated)
    }

    suspend fun notifyProfileImageUpdate(newLocalUri: String): SubscriptionReceiveChannel<Int?>{
        return mModel.updateUserProfileImage(newLocalUri)
    }

    suspend fun notifyBackgroundImageUpdate(newLocalUri: String): SubscriptionReceiveChannel<Int?> {
        return mModel.updateUserBackgroundImage(newLocalUri)
    }
}