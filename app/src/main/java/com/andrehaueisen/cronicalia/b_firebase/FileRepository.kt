package com.andrehaueisen.cronicalia.b_firebase

import android.net.Uri
import android.support.v4.util.ArraySet
import android.util.Log
import com.andrehaueisen.cronicalia.*
import com.andrehaueisen.cronicalia.models.Book
import com.andrehaueisen.cronicalia.models.User
import com.andrehaueisen.cronicalia.utils.extensions.isUriFromFirebaseStorage
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageException
import com.google.firebase.storage.StorageMetadata
import com.google.firebase.storage.UploadTask
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.channels.ArrayBroadcastChannel
import kotlinx.coroutines.experimental.channels.SubscriptionReceiveChannel
import kotlinx.coroutines.experimental.launch
import kotlin.coroutines.experimental.suspendCoroutine


/**
 * Created by andre on 2/18/2018.
 */
class FileRepository(
    private val mStorageInstance: FirebaseStorage,
    private val mUser: User
) {

    private var mTasksReadyCounter = 0

    suspend fun createBook(book: Book, dataRepository: DataRepository): SubscriptionReceiveChannel<Int> {

        val progressBroadcastChannel = ArrayBroadcastChannel<Int>(4)
        val progressReceiver = progressBroadcastChannel.openSubscription()

        var uploadTask: UploadTask
        val progressCap = getProgressCapForCreation(book)
        var currentProgress = 0.0
        val maxTaskCount = (progressCap).toInt()

        val locationReference = mStorageInstance.reference
            .child(book.getStorageRootLocation())
            .child(mUser.encodedEmail!!)
            .child(book.generateBookKey())

        fun uploadPdfs() {

            if (book.isLaunchedComplete) {
                val filePath = Uri.parse(book.localFullBookUri!!)
                uploadTask = locationReference.child("${book.originalImmutableTitle}.pdf".replace(" ", "")).putFile(filePath)
                uploadTask.addOnProgressListener { taskSnapshot ->
                    launch {
                        currentProgress += (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount) / progressCap
                        if (currentProgress < 99)
                            progressBroadcastChannel.send(currentProgress.toInt())
                        else
                            progressBroadcastChannel.send(99)
                    }

                }.addOnSuccessListener { taskSnapshot ->
                    book.remoteFullBookUri = taskSnapshot.downloadUrl?.toString()
                    saveBookOnDatabaseIfAllFilesUploaded(maxTaskCount, book, dataRepository, progressBroadcastChannel)

                }.addOnFailureListener { exception ->
                    launch {
                        val errorCode = (exception as StorageException).errorCode
                        when (errorCode) {
                            StorageException.ERROR_UNKNOWN -> progressBroadcastChannel.send(
                                errorCode
                            )
                            else -> progressBroadcastChannel.send(UPLOAD_STATUS_FAIL)
                        }

                    }
                }

            } else {
                book.remoteChapterUris.forEachIndexed { index, key ->

                    val filePath = Uri.parse(key)
                    val chapterNumberMetadata =
                        StorageMetadata.Builder().setCustomMetadata(METADATA_CHAPTER_NUMBER, index.toString()).build()
                    val chapterTitle = book.remoteChapterTitles[index]
                    uploadTask = locationReference.child("$chapterTitle.pdf").putFile(filePath, chapterNumberMetadata)
                    uploadTask.addOnProgressListener { taskSnapshot ->
                        launch(UI) {
                            currentProgress += (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount) / progressCap
                            if (currentProgress < 99)
                                progressBroadcastChannel.send(currentProgress.toInt())
                            else
                                progressBroadcastChannel.send(99)

                        }
                    }.addOnSuccessListener { taskSnapshot ->
                        val uri = taskSnapshot.downloadUrl
                        uri?.let {
                            book.remoteChapterTitles.set(index, chapterTitle)
                            book.remoteChapterUris.set(index, it.toString())
                        }
                        saveBookOnDatabaseIfAllFilesUploaded(maxTaskCount, book, dataRepository, progressBroadcastChannel)
                    }.addOnFailureListener { exception ->
                        launch {
                            val errorCode = (exception as StorageException).errorCode
                            when (errorCode) {
                                StorageException.ERROR_UNKNOWN -> progressBroadcastChannel.send(
                                    errorCode
                                )
                                else -> progressBroadcastChannel.send(UPLOAD_STATUS_FAIL)
                            }

                        }
                    }
                }

            }
        }

        fun uploadImages() {

            book.localCoverUri?.let { coverUri ->
                val metadata = StorageMetadata.Builder()
                    .setCustomMetadata(
                        METADATA_TITLE_IMAGE_TYPE,
                        METADATA_PROPERTY_IMAGE_TYPE_COVER
                    )
                    .build()

                val filePath = Uri.parse(coverUri)
                uploadTask = locationReference.child(filePath.lastPathSegment).putFile(filePath, metadata)

                uploadTask.addOnProgressListener { taskSnapshot ->
                    launch(UI) {
                        currentProgress += (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount) / progressCap
                        if (currentProgress < 99)
                            progressBroadcastChannel.send(currentProgress.toInt())
                        else
                            progressBroadcastChannel.send(99)
                    }
                }.addOnSuccessListener { taskSnapshot ->
                    book.remoteCoverUri = taskSnapshot.downloadUrl?.toString()
                    saveBookOnDatabaseIfAllFilesUploaded(maxTaskCount, book, dataRepository, progressBroadcastChannel)
                }.addOnFailureListener { exception ->
                    launch {
                        val errorCode = (exception as StorageException).errorCode
                        when (errorCode) {
                            StorageException.ERROR_UNKNOWN -> progressBroadcastChannel.send(errorCode)
                            else -> progressBroadcastChannel.send(UPLOAD_STATUS_FAIL)
                        }
                    }
                }
            }

            book.localPosterUri?.let { posterUri ->
                val metadata = StorageMetadata.Builder()
                    .setCustomMetadata(
                        METADATA_TITLE_IMAGE_TYPE,
                        METADATA_PROPERTY_IMAGE_TYPE_POSTER
                    )
                    .build()

                val filePath = Uri.parse(posterUri)
                uploadTask = locationReference.child(filePath.lastPathSegment).putFile(filePath, metadata)

                uploadTask.addOnProgressListener { taskSnapshot ->
                    launch(UI) {
                        currentProgress += (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount) / progressCap
                        if (currentProgress < 99)
                            progressBroadcastChannel.send(currentProgress.toInt())
                        else
                            progressBroadcastChannel.send(99)
                    }
                }.addOnSuccessListener { taskSnapshot ->
                    book.remotePosterUri = taskSnapshot.downloadUrl?.toString()
                    saveBookOnDatabaseIfAllFilesUploaded(maxTaskCount, book, dataRepository, progressBroadcastChannel)
                }.addOnFailureListener { exception ->
                    launch {
                        val errorCode = (exception as StorageException).errorCode
                        when (errorCode) {
                            StorageException.ERROR_UNKNOWN -> progressBroadcastChannel.send(
                                errorCode
                            )
                            else -> progressBroadcastChannel.send(UPLOAD_STATUS_FAIL)
                        }
                    }
                }
            }
        }

        uploadPdfs()
        uploadImages()

        return progressReceiver
    }

    suspend fun updateBookCover(book: Book, dataRepository: DataRepository): Int {

        var uploadTask: UploadTask

        val locationReference = mStorageInstance.reference
            .child(book.getStorageRootLocation())
            .child(mUser.encodedEmail!!)
            .child(book.generateBookKey())

        return async(CommonPool) {
            suspendCoroutine<Int> { continuation ->

                if (book.localCoverUri != null) {

                    val metadata = StorageMetadata.Builder()
                        .setCustomMetadata(
                            METADATA_TITLE_IMAGE_TYPE,
                            METADATA_PROPERTY_IMAGE_TYPE_COVER
                        )
                        .build()

                    val filePath = Uri.parse(book.localCoverUri)
                    uploadTask = locationReference.child(filePath.lastPathSegment).putFile(filePath, metadata)

                    uploadTask.addOnSuccessListener { taskSnapshot ->
                        book.remoteCoverUri = taskSnapshot.downloadUrl?.toString()
                        dataRepository.setBookDocuments(book, continuation)

                    }.addOnFailureListener { exception ->

                        val errorCode = (exception as StorageException).errorCode
                        when (errorCode) {
                            StorageException.ERROR_UNKNOWN -> continuation.resume(errorCode)
                            else -> continuation.resume(UPLOAD_STATUS_FAIL)
                        }
                    }
                } else {
                    continuation.resume(UPLOAD_STATUS_FAIL)
                }
            }
        }.await()


    }

    suspend fun updateBookPoster(book: Book, dataRepository: DataRepository): Int {

        var uploadTask: UploadTask

        val locationReference = mStorageInstance.reference
            .child(book.getStorageRootLocation())
            .child(mUser.encodedEmail!!)
            .child(book.generateBookKey())

        return async(CommonPool) {
            suspendCoroutine<Int> { continuation ->
                if (book.localPosterUri != null) {
                    val metadata = StorageMetadata.Builder()
                        .setCustomMetadata(
                            METADATA_TITLE_IMAGE_TYPE,
                            METADATA_PROPERTY_IMAGE_TYPE_POSTER
                        )
                        .build()

                    val filePath = Uri.parse(book.localPosterUri)
                    uploadTask = locationReference.child(filePath.lastPathSegment).putFile(filePath, metadata)

                    uploadTask.addOnSuccessListener { taskSnapshot ->
                        book.remotePosterUri = taskSnapshot.downloadUrl?.toString()
                        dataRepository.setBookDocuments(book, continuation = continuation)

                    }.addOnFailureListener { exception ->
                        val errorCode = (exception as StorageException).errorCode
                        when (errorCode) {
                            StorageException.ERROR_UNKNOWN -> continuation.resume(errorCode)
                            else -> continuation.resume(UPLOAD_STATUS_FAIL)
                        }
                    }
                } else {
                    continuation.resume(UPLOAD_STATUS_FAIL)
                }
            }
        }.await()
    }

    suspend fun updateUserProfileImage(newLocalUri: String, dataRepository: DataRepository): Int {

        val uploadTask: UploadTask

        val locationReference = mStorageInstance.reference
            .child(STORAGE_USERS)
            .child(mUser.encodedEmail!!)


        val metadata = StorageMetadata.Builder()
            .setCustomMetadata(
                METADATA_TITLE_IMAGE_TYPE,
                METADATA_PROPERTY_IMAGE_TYPE_PROFILE
            )
            .build()

        val filePath = Uri.parse(newLocalUri)
        uploadTask = locationReference.child(filePath.lastPathSegment).putFile(filePath, metadata)

        return async(CommonPool) {
            suspendCoroutine<Int> { continuation ->
                uploadTask.addOnSuccessListener { taskSnapshot ->
                    mUser.remoteProfilePictureUri = taskSnapshot.downloadUrl?.toString()
                    dataRepository.updateUserProfilePictureReferences(newLocalUri, mUser.remoteProfilePictureUri!!)
                    continuation.resume(UPLOAD_STATUS_OK)

                }.addOnFailureListener { exception ->
                    val errorCode = (exception as StorageException).errorCode
                    when (errorCode) {
                        StorageException.ERROR_UNKNOWN -> continuation.resume(errorCode)
                        else -> continuation.resume(UPLOAD_STATUS_FAIL)
                    }

                }
            }
        }.await()

    }

    suspend fun updateUserBackgroundImage(newLocalUri: String, dataRepository: DataRepository): Int {
        val uploadTask: UploadTask

        val locationReference = mStorageInstance.reference
            .child(STORAGE_USERS)
            .child(mUser.encodedEmail!!)


        val metadata = StorageMetadata.Builder()
            .setCustomMetadata(
                METADATA_TITLE_IMAGE_TYPE,
                METADATA_PROPERTY_IMAGE_TYPE_BACKGROUND
            )
            .build()

        val filePath = Uri.parse(newLocalUri)
        uploadTask = locationReference.child(filePath.lastPathSegment).putFile(filePath, metadata)

        return async(CommonPool) {
            suspendCoroutine<Int> { continuation ->
                uploadTask.addOnSuccessListener { taskSnapshot ->

                    mUser.remoteBackgroundPictureUri = taskSnapshot.downloadUrl?.toString()
                    dataRepository.updateUserBackgroundPictureReferences(newLocalUri, mUser.remoteBackgroundPictureUri!!)
                    continuation.resume(UPLOAD_STATUS_OK)

                }.addOnFailureListener { exception ->

                    val errorCode = (exception as StorageException).errorCode
                    when (errorCode) {
                        StorageException.ERROR_UNKNOWN -> continuation.resume(errorCode)
                        else -> continuation.resume(UPLOAD_STATUS_FAIL)
                    }
                }
            }
        }.await()

    }

    suspend fun updatePdfs(
        book: Book,
        dataRepository: DataRepository,
        filesToBeDeleted: ArraySet<String>
    ): SubscriptionReceiveChannel<Int> {

        val progressBroadcastChannel = ArrayBroadcastChannel<Int>(4)
        val progressReceiver = progressBroadcastChannel.openSubscription()

        deleteBookFiles(filesToBeDeleted)

        var uploadTask: UploadTask
        val progressCap = getProgressCapForEdition(book)
        var currentProgress = 0.0
        val maxTaskCount = (progressCap).toInt()

        val locationReference = mStorageInstance.reference
            .child(book.getStorageRootLocation())
            .child(mUser.encodedEmail!!)
            .child(book.generateBookKey())

        if (book.isLaunchedComplete) {

            val filePath = Uri.parse(book.localFullBookUri!!)
            uploadTask = locationReference.child("${book.originalImmutableTitle}.pdf".replace(" ", ""))
                .putFile(filePath)
            uploadTask.addOnProgressListener { taskSnapshot ->
                launch {
                    currentProgress += (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount) / progressCap
                    if (currentProgress < 99)
                        progressBroadcastChannel.send(currentProgress.toInt())
                    else
                        progressBroadcastChannel.send(99)
                }

            }.addOnSuccessListener { taskSnapshot ->
                book.remoteFullBookUri = taskSnapshot.downloadUrl?.toString()
                saveBookOnDatabaseIfAllFilesUploaded(maxTaskCount, book, dataRepository, progressBroadcastChannel)
            }.addOnFailureListener { exception ->
                launch {
                    val errorCode = (exception as StorageException).errorCode
                    when (errorCode) {
                        StorageException.ERROR_UNKNOWN -> progressBroadcastChannel.send(
                            errorCode
                        )
                        else -> progressBroadcastChannel.send(UPLOAD_STATUS_FAIL)
                    }

                }
            }

        } else {
            var editionFound = false

            book.remoteChapterUris.forEachIndexed { index, key ->

                if (!key.isUriFromFirebaseStorage()) {
                    editionFound = true

                    val filePath = Uri.parse(key)
                    val chapterNumberMetadata =
                        StorageMetadata.Builder().setCustomMetadata(METADATA_CHAPTER_NUMBER, index.toString()).build()
                    val chapterTitle = book.remoteChapterTitles[index]

                    uploadTask = locationReference.child("$chapterTitle.pdf").putFile(filePath, chapterNumberMetadata)
                    uploadTask.addOnProgressListener { taskSnapshot ->
                        launch(UI) {
                            currentProgress += (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount) / progressCap
                            if (currentProgress < 99)
                                progressBroadcastChannel.send(currentProgress.toInt())
                            else
                                progressBroadcastChannel.send(99)

                        }
                    }.addOnSuccessListener { taskSnapshot ->
                        val uri = taskSnapshot.downloadUrl
                        uri?.let {
                            book.remoteChapterTitles.set(index, chapterTitle)
                            book.remoteChapterUris.set(index, it.toString())
                        }
                        saveBookOnDatabaseIfAllFilesUploaded(maxTaskCount, book, dataRepository, progressBroadcastChannel)
                    }.addOnFailureListener { exception ->
                        launch {
                            val errorCode = (exception as StorageException).errorCode
                            when (errorCode) {
                                StorageException.ERROR_UNKNOWN -> progressBroadcastChannel.send(
                                    errorCode
                                )
                                else -> progressBroadcastChannel.send(UPLOAD_STATUS_FAIL)
                            }

                        }
                    }
                }
            }

            if (!editionFound) {
                saveBookOnDatabaseIfAllFilesUploaded(1, book, dataRepository, progressBroadcastChannel)
            }
        }

        return progressReceiver
    }

    fun deleteFullBook(book: Book, dataRepository: DataRepository) {
        val filesToBeDeleted = ArraySet<String>()

        book.remoteCoverUri?.let {
            filesToBeDeleted.add(book.remoteCoverUri!!)
        }
        book.remotePosterUri?.let {
            filesToBeDeleted.add(book.remotePosterUri!!)
        }

        if (book.isLaunchedComplete) {
            filesToBeDeleted.add(book.remoteFullBookUri!!)

        } else {
            filesToBeDeleted.addAll(book.remoteChapterUris)
        }

        deleteBookFiles(filesToBeDeleted)
        dataRepository.deleteBook(book)
    }

    private fun deleteBookFiles(filesToBeDeleted: ArraySet<String>) {
        filesToBeDeleted.forEach { fileUri ->
            mStorageInstance.getReferenceFromUrl(fileUri).delete()
        }
    }


    private fun getProgressCapForCreation(book: Book): Double {
        var progressCap = 0.0

        book.localPosterUri?.let { progressCap += 1 }
        book.localCoverUri?.let { progressCap += 1 }
        if (book.isLaunchedComplete)
            progressCap += 1
        else
            book.remoteChapterTitles.forEach { progressCap += 1 }

        return progressCap
    }

    private fun getProgressCapForEdition(book: Book): Double {
        var progressCap = 0.0

        if (book.isLaunchedComplete) {
            progressCap += 1
        } else {
            book.remoteChapterUris.forEach { uri ->
                if (!uri.isUriFromFirebaseStorage()) {
                    progressCap += 1
                }
            }
        }

        return progressCap
    }

    private fun saveBookOnDatabaseIfAllFilesUploaded(
        maxTasksNumber: Int,
        book: Book,
        dataRepository: DataRepository,
        progressBroadcastChannel: ArrayBroadcastChannel<Int>?
    ) {

        mTasksReadyCounter++

        Log.i("FileRepository", "Tasks counter: $mTasksReadyCounter Max Tasks: $maxTasksNumber")
        if (mTasksReadyCounter == maxTasksNumber) {
            dataRepository.setBookDocuments(book, progressBroadcastChannel = progressBroadcastChannel)
            mTasksReadyCounter = 0
        }

    }
}