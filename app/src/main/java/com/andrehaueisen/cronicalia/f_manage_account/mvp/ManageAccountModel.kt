package com.andrehaueisen.cronicalia.f_manage_account.mvp

import com.andrehaueisen.cronicalia.b_firebase.DataRepository
import com.andrehaueisen.cronicalia.b_firebase.FileRepository
import kotlinx.coroutines.experimental.channels.SubscriptionReceiveChannel

class ManageAccountModel(private val mDataRepository: DataRepository, private val mFileRepository: FileRepository) {

    enum class SimpleUserUpdateVariable{
        NAME, ARTISTIC_NAME, ABOUT_ME
    }

    fun simpleUpdateBook(textUpdate: String, variableToUpdate: SimpleUserUpdateVariable){

        when(variableToUpdate){
            SimpleUserUpdateVariable.NAME -> mDataRepository.updateUserName(newName = textUpdate)
            SimpleUserUpdateVariable.ARTISTIC_NAME -> mDataRepository.updateUserArtisticName(newArtisticName = textUpdate)
            SimpleUserUpdateVariable.ABOUT_ME -> mDataRepository.updateUserAboutMe(newAboutMe = textUpdate)

        }
    }

    suspend fun updateUserProfileImage(newLocalUri: String): SubscriptionReceiveChannel<Int?> {
        return mFileRepository.updateUserProfileImage(newLocalUri, mDataRepository)
    }

    suspend fun updateUserBackgroundImage(newLocalUri: String): SubscriptionReceiveChannel<Int?>{
        return mFileRepository.updateUserBackgroundImage(newLocalUri, mDataRepository)
    }
}