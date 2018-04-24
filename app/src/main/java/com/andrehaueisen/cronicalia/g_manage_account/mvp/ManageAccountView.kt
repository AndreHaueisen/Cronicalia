package com.andrehaueisen.cronicalia.g_manage_account.mvp

import android.net.Uri
import android.os.Bundle
import com.andrehaueisen.cronicalia.*
import com.andrehaueisen.cronicalia.g_manage_account.EditTextDialog
import com.andrehaueisen.cronicalia.models.User
import com.andrehaueisen.cronicalia.utils.extensions.createUserDirectory
import com.andrehaueisen.cronicalia.utils.extensions.isOnline
import com.andrehaueisen.cronicalia.utils.extensions.showRequestFeedback
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.g_activity_manage_account.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import java.io.File

class ManageAccountView(private val mActivity: ManageAccountPresenterActivity, user: User, imageDestination: String = ImageDestination.PROFILE.toString()) :
    EditTextDialog.UserChangesListener {

    private val mUserIsolated: User = user.copy()
    private var mImageDestination = when (imageDestination) {
        ImageDestination.PROFILE.toString() -> ImageDestination.PROFILE
        ImageDestination.BACKGROUND.toString() -> ImageDestination.BACKGROUND
        else -> ImageDestination.PROFILE
    }

    init {
        initiateImageViews()
        initiateButtons()
        initiateNameTextView()
        initiateArtisticNameView()
        initiateAboutMeTextView()
        initiateExtraInfoTextViews()
    }

    enum class ImageDestination {
        PROFILE, BACKGROUND
    }

    private fun initiateImageViews() {
        placeBackgroundImage()
        placeProfileImage()

    }

    private fun initiateButtons() {

        mActivity.change_background_picture_button.setOnClickListener {
            val file: File
            val directoryName = "profile"

            file = if (mUserIsolated.localBackgroundPictureUri != null) {
                mActivity
                    .cacheDir
                    .createUserDirectory(directoryName, FILE_NAME_BACKGROUND_PICTURE)
            } else {
                mActivity.cacheDir.createUserDirectory(directoryName, FILE_NAME_BACKGROUND_PICTURE)
            }

            CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setActivityTitle(mActivity.getString(R.string.background_image))
                .setAspectRatio(16, 9)
                .setOutputUri(Uri.fromFile(file))
                .start(mActivity)

            mImageDestination = ImageDestination.BACKGROUND
        }

        mActivity.change_profile_picture_button.setOnClickListener {
            val file: File
            val directoryName = "profile"

            file = if (mUserIsolated.localProfilePictureUri != null) {
                mActivity
                    .cacheDir
                    .createUserDirectory(directoryName, FILE_NAME_PROFILE_PICTURE)
            } else {
                mActivity.cacheDir.createUserDirectory(directoryName, FILE_NAME_PROFILE_PICTURE)
            }

            CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setActivityTitle(mActivity.getString(R.string.profile_image))
                .setAspectRatio(1, 1)
                .setCropShape(CropImageView.CropShape.OVAL)
                .setOutputUri(Uri.fromFile(file))
                .start(mActivity)

            mImageDestination = ImageDestination.PROFILE
        }
    }

    private fun initiateNameTextView() {
        mActivity.user_name_text_view.text = mUserIsolated.name
        mActivity.user_name_text_view.setOnClickListener {
            EditTextDialog(mActivity, this, EditTextDialog.ViewBeingEdited.NAME_VIEW, mUserIsolated).show()
        }
    }

    private fun initiateArtisticNameView() {
        mActivity.artistic_name_text_view.text = mUserIsolated.artisticName ?: mActivity.getString(R.string.artistic_name_instructions)
        mActivity.artistic_name_text_view.setOnClickListener {
            EditTextDialog(mActivity, this, EditTextDialog.ViewBeingEdited.ARTISTIC_NAME, mUserIsolated).show()
        }
    }

    private fun initiateAboutMeTextView() {
        mActivity.about_me_text_view.text = mUserIsolated.aboutMe ?: mActivity.getString(R.string.about_me_instructions)
        mActivity.about_me_text_view.setOnClickListener {
            EditTextDialog(mActivity, this, EditTextDialog.ViewBeingEdited.ABOUT_ME_VIEW, mUserIsolated).show()
        }
    }

    private fun initiateExtraInfoTextViews() {

        with(mActivity) {
            fans_text_view.text = getString(R.string.simple_number_integer, mUserIsolated.fans)
            total_readings_number_text_view.text = getString(R.string.simple_number_integer, mUserIsolated.getTotalReadings())
            total_income_text_view.text = getString(R.string.income_amount, mUserIsolated.getTotalIncome())
        }

    }

    fun onImageReady(pictureUri: Uri) {

        with(mActivity) {
            when (mImageDestination) {
                ImageDestination.BACKGROUND -> {
                    mUserIsolated.localBackgroundPictureUri = pictureUri.toString()
                    notifyImageUpdateAndListenToResult(mUserIsolated.localBackgroundPictureUri!!, ImageDestination.BACKGROUND)
                    placeBackgroundImage()
                }
                ImageDestination.PROFILE -> {
                    mUserIsolated.localProfilePictureUri = pictureUri.toString()
                    notifyImageUpdateAndListenToResult(mUserIsolated.localProfilePictureUri!!, ImageDestination.PROFILE)
                    placeProfileImage()
                }
            }

            if (isOnline()) {
                Toasty.info(this, mActivity.getString(R.string.updating_image)).show()
            } else {
                Toasty.info(this, getString(R.string.update_begin_once_internet_available)).show()
            }
        }

    }

    private fun notifyImageUpdateAndListenToResult(newLocalUri: String, imageDestination: ImageDestination) {

        with(mActivity) {
            launch(UI) {
                val result = when (imageDestination) {
                    ImageDestination.BACKGROUND -> {
                        requestBackgroundImageUpdate(newLocalUri)
                    }
                    ImageDestination.PROFILE -> {
                        requestProfileImageUpdate(newLocalUri)
                    }
                }

                result.showRequestFeedback(mActivity, R.string.image_upload_successful, R.string.image_upload_failed)
            }
        }
    }

    private fun placeBackgroundImage() {

        mUserIsolated.localBackgroundPictureUri?.let {
            val requestOptions = RequestOptions
                .diskCacheStrategyOf(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)

            val transitionOptions = DrawableTransitionOptions.withCrossFade()

            Glide.with(mActivity)
                .load(Uri.parse(mUserIsolated.localBackgroundPictureUri))
                .error(Glide.with(mActivity).load(mUserIsolated.remoteBackgroundPictureUri))
                .apply(requestOptions)
                .transition(transitionOptions)
                .into(mActivity.user_background_image_view)
        }
    }

    private fun placeProfileImage() {

        mUserIsolated.localProfilePictureUri?.let {
            val requestOptions = RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE).skipMemoryCache(true)

            Glide.with(mActivity)
                .load(Uri.parse(mUserIsolated.localProfilePictureUri))
                .error(Glide.with(mActivity).load(mUserIsolated.remoteProfilePictureUri))
                .apply(requestOptions)
                .into(mActivity.profile_image_view)
        }

    }

    override fun notifyNameChange(name: String) {
        mActivity.user_name_text_view.text = name
        mActivity.notifySimpleUpdate(name, ManageAccountModel.SimpleUserUpdateVariable.NAME)
    }

    override fun notifyArtisticNameChange(artisticName: String) {
        mActivity.artistic_name_text_view.text = artisticName
        mActivity.notifySimpleUpdate(artisticName, ManageAccountModel.SimpleUserUpdateVariable.ARTISTIC_NAME)
    }

    override fun notifyAboutMeChange(aboutMe: String) {
        mActivity.about_me_text_view.text = aboutMe
        mActivity.notifySimpleUpdate(aboutMe, ManageAccountModel.SimpleUserUpdateVariable.ABOUT_ME)
    }

    fun onSaveInstanceState(bundle: Bundle): Bundle {
        bundle.putParcelable(PARCELABLE_USER, mUserIsolated)
        bundle.putString(PARCELABLE_IMAGE_DESTINATION, mImageDestination.toString())
        return bundle
    }

    fun onError(errorDescription: String) {
        Toasty.error(mActivity, errorDescription).show()
    }
}