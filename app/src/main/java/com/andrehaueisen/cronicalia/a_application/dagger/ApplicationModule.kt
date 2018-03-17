package com.andrehaueisen.cronicalia.a_application.dagger

import android.content.Context
import com.andrehaueisen.cronicalia.b_firebase.Authenticator
import com.andrehaueisen.cronicalia.b_firebase.DataRepository
import com.andrehaueisen.cronicalia.b_firebase.FileRepository
import com.andrehaueisen.cronicalia.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.StorageReference
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.experimental.channels.ArrayBroadcastChannel
import kotlinx.coroutines.experimental.channels.SubscriptionReceiveChannel

/**
 * Created by andre on 2/18/2018.
 */
@Module
class ApplicationModule(
    private val mStorageReference: StorageReference,
    private val mDatabaseInstance: FirebaseFirestore,
    private val mFirebaseAuth: FirebaseAuth,
    private val mGlobalProgressBroadcastChannel: ArrayBroadcastChannel<Int?>,
    private val mGlobalProgressReceiver: SubscriptionReceiveChannel<Int?>,
    private val mUser: User
) {

    @ApplicationScope
    @Provides
    fun provideFileRepository() =
        FileRepository(
            mStorageReference,
            mGlobalProgressBroadcastChannel,
            mGlobalProgressReceiver,
            mUser
        )

    @ApplicationScope
    @Provides
    fun provideDataRepository() = DataRepository(mDatabaseInstance, mGlobalProgressBroadcastChannel, mGlobalProgressReceiver, mUser)

    @ApplicationScope
    @Provides
    fun provideAuthenticator(context: Context) = Authenticator(context, mDatabaseInstance, mFirebaseAuth, mUser)

    @ApplicationScope
    @Provides
    fun provideUser() = mUser

}