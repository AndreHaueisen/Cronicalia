package com.andrehaueisen.cronicalia.g_manage_account.mvp

import com.andrehaueisen.cronicalia.b_firebase.DataRepository
import com.andrehaueisen.cronicalia.b_firebase.FileRepository

class ManageAccountModel(private val mDataRepository: DataRepository, private val mFileRepository: FileRepository) {

    enum class SimpleUserUpdateVariable{
        NAME, TWITTER_PROFILE, ABOUT_ME
    }

    fun simpleUpdateBook(textUpdate: String, variableToUpdate: SimpleUserUpdateVariable){

        when(variableToUpdate){
            SimpleUserUpdateVariable.NAME -> mDataRepository.updateUserName(newName = textUpdate)
            SimpleUserUpdateVariable.TWITTER_PROFILE -> mDataRepository.updateUserTwitterProfile(twitterProfile = textUpdate)
            SimpleUserUpdateVariable.ABOUT_ME -> mDataRepository.updateUserAboutMe(newAboutMe = textUpdate)

        }
    }

    suspend fun updateUserProfileImage(newLocalUri: String): Int {
        return mFileRepository.updateUserProfileImage(newLocalUri, mDataRepository)
    }

    suspend fun updateUserBackgroundImage(newLocalUri: String): Int{
        return mFileRepository.updateUserBackgroundImage(newLocalUri, mDataRepository)
    }
}