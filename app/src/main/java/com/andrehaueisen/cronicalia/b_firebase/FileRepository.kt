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
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.channels.ArrayBroadcastChannel
import kotlinx.coroutines.experimental.channels.SubscriptionReceiveChannel
import kotlinx.coroutines.experimental.launch


/**
 * Created by andre on 2/18/2018.
 */
class FileRepository(
    private val mStorageInstance: FirebaseStorage,
    private val mGlobalProgressBroadcastChannel: ArrayBroadcastChannel<Int?>,
    private val mGlobalProgressReceiver: SubscriptionReceiveChannel<Int?>,
    private val mUser: User
) {

    private var mTasksReadyCounter = 0

    suspend fun createBook(book: Book, dataRepository: DataRepository): SubscriptionReceiveChannel<Int?> {

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
                uploadTask = locationReference.child("${book.originalImmutableTitle}.pdf".replace(" ", ""))
                    .putFile(filePath)
                uploadTask.addOnProgressListener { taskSnapshot ->
                    launch {
                        currentProgress += (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount) / progressCap
                        if (currentProgress < 99)
                            mGlobalProgressBroadcastChannel.send(currentProgress.toInt())
                        else
                            mGlobalProgressBroadcastChannel.send(99)
                    }

                }.addOnSuccessListener { taskSnapshot ->
                    book.remoteFullBookUri = taskSnapshot.downloadUrl?.toString()
                    saveBookOnDatabaseIfAllFilesUploaded(maxTaskCount, book, dataRepository)
                }.addOnFailureListener { exception ->
                    launch {
                        val errorCode = (exception as StorageException).errorCode
                        when (errorCode) {
                            StorageException.ERROR_UNKNOWN -> mGlobalProgressBroadcastChannel.send(
                                errorCode
                            )
                            else -> mGlobalProgressBroadcastChannel.send(UPLOAD_STATUS_FAIL)
                        }

                    }
                }

            } else {
                book.remoteChapterUris.forEachIndexed { index, key ->

                    val filePath = Uri.parse(key)
                    val chapterNumberMetadata = StorageMetadata.Builder().setCustomMetadata(METADATA_CHAPTER_NUMBER, index.toString()).build()
                    val chapterTitle = book.remoteChapterTitles[index]
                    uploadTask = locationReference.child("$chapterTitle.pdf").putFile(filePath, chapterNumberMetadata)
                    uploadTask.addOnProgressListener { taskSnapshot ->
                        launch(UI) {
                            currentProgress += (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount) / progressCap
                            if (currentProgress < 99)
                                mGlobalProgressBroadcastChannel.send(currentProgress.toInt())
                            else
                                mGlobalProgressBroadcastChannel.send(99)

                        }
                    }.addOnSuccessListener { taskSnapshot ->
                        val uri = taskSnapshot.downloadUrl
                        uri?.let {
                            book.remoteChapterTitles.set(index, chapterTitle)
                            book.remoteChapterUris.set(index, it.toString())
                        }
                        saveBookOnDatabaseIfAllFilesUploaded(maxTaskCount, book, dataRepository)
                    }.addOnFailureListener { exception ->
                        launch {
                            val errorCode = (exception as StorageException).errorCode
                            when (errorCode) {
                                StorageException.ERROR_UNKNOWN -> mGlobalProgressBroadcastChannel.send(
                                    errorCode
                                )
                                else -> mGlobalProgressBroadcastChannel.send(UPLOAD_STATUS_FAIL)
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
                            mGlobalProgressBroadcastChannel.send(currentProgress.toInt())
                        else
                            mGlobalProgressBroadcastChannel.send(99)
                    }
                }.addOnSuccessListener { taskSnapshot ->
                    book.remoteCoverUri = taskSnapshot.downloadUrl?.toString()
                    saveBookOnDatabaseIfAllFilesUploaded(maxTaskCount, book, dataRepository)
                }.addOnFailureListener { exception ->
                    launch {
                        val errorCode = (exception as StorageException).errorCode
                        when (errorCode) {
                            StorageException.ERROR_UNKNOWN -> mGlobalProgressBroadcastChannel.send(errorCode)
                            else -> mGlobalProgressBroadcastChannel.send(UPLOAD_STATUS_FAIL)
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
                            mGlobalProgressBroadcastChannel.send(currentProgress.toInt())
                        else
                            mGlobalProgressBroadcastChannel.send(99)
                    }
                }.addOnSuccessListener { taskSnapshot ->
                    book.remotePosterUri = taskSnapshot.downloadUrl?.toString()
                    saveBookOnDatabaseIfAllFilesUploaded(maxTaskCount, book, dataRepository)
                }.addOnFailureListener { exception ->
                    launch {
                        val errorCode = (exception as StorageException).errorCode
                        when (errorCode) {
                            StorageException.ERROR_UNKNOWN -> mGlobalProgressBroadcastChannel.send(
                                errorCode
                            )
                            else -> mGlobalProgressBroadcastChannel.send(UPLOAD_STATUS_FAIL)
                        }
                    }
                }
            }
        }

        uploadPdfs()
        uploadImages()

        return mGlobalProgressReceiver
    }

    suspend fun updateBookCover(book: Book, dataRepository: DataRepository): SubscriptionReceiveChannel<Int?> {

        var uploadTask: UploadTask

        val locationReference = mStorageInstance.reference
            .child(book.getStorageRootLocation())
            .child(mUser.encodedEmail!!)
            .child(book.generateBookKey())

        book.localCoverUri?.let { coverUri ->
            val metadata = StorageMetadata.Builder()
                .setCustomMetadata(
                    METADATA_TITLE_IMAGE_TYPE,
                    METADATA_PROPERTY_IMAGE_TYPE_COVER
                )
                .build()

            val filePath = Uri.parse(coverUri)
            uploadTask = locationReference.child(filePath.lastPathSegment).putFile(filePath, metadata)

            uploadTask.addOnSuccessListener { taskSnapshot ->
                launch {
                    book.remoteCoverUri = taskSnapshot.downloadUrl?.toString()
                    dataRepository.setBookDocuments(book)
                }

            }.addOnFailureListener { exception ->
                launch {
                    val errorCode = (exception as StorageException).errorCode
                    when (errorCode) {
                        StorageException.ERROR_UNKNOWN -> mGlobalProgressBroadcastChannel.send(errorCode)
                        else -> mGlobalProgressBroadcastChannel.send(UPLOAD_STATUS_FAIL)
                    }
                }
            }
        }

        return mGlobalProgressReceiver
    }

    suspend fun updateBookPoster(book: Book, dataRepository: DataRepository): SubscriptionReceiveChannel<Int?> {

        var uploadTask: UploadTask

        val locationReference = mStorageInstance.reference
            .child(book.getStorageRootLocation())
            .child(mUser.encodedEmail!!)
            .child(book.generateBookKey())

        book.localPosterUri?.let { posterUri ->
            val metadata = StorageMetadata.Builder()
                .setCustomMetadata(
                    METADATA_TITLE_IMAGE_TYPE,
                    METADATA_PROPERTY_IMAGE_TYPE_POSTER
                )
                .build()

            val filePath = Uri.parse(posterUri)
            uploadTask = locationReference.child(filePath.lastPathSegment).putFile(filePath, metadata)

            uploadTask.addOnSuccessListener { taskSnapshot ->
                book.remotePosterUri = taskSnapshot.downloadUrl?.toString()
                dataRepository.setBookDocuments(book)

            }.addOnFailureListener { exception ->
                launch {
                    val errorCode = (exception as StorageException).errorCode
                    when (errorCode) {
                        StorageException.ERROR_UNKNOWN -> mGlobalProgressBroadcastChannel.send(errorCode)
                        else -> mGlobalProgressBroadcastChannel.send(UPLOAD_STATUS_FAIL)
                    }
                }
            }
        }

        return mGlobalProgressReceiver
    }

    suspend fun updatePdfs(book: Book, dataRepository: DataRepository, filesToBeDeleted: ArraySet<String>): SubscriptionReceiveChannel<Int?> {

        deleteBookFiles(filesToBeDeleted)

        var uploadTask: UploadTask
        val progressCap = getProgressCapForEdition(book)
        var currentProgress = 0.0
        val maxTaskCount = (progressCap).toInt()

        val locationReference = mStorageInstance.reference
            .child(book.getStorageRootLocation())
            .child(mUser.encodedEmail!!)
            .child(book.generateBookKey())

        if (book.isLaunchedComplete){

            val filePath = Uri.parse(book.localFullBookUri!!)
            uploadTask = locationReference.child("${book.originalImmutableTitle}.pdf".replace(" ", ""))
                .putFile(filePath)
            uploadTask.addOnProgressListener { taskSnapshot ->
                launch {
                    currentProgress += (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount) / progressCap
                    if (currentProgress < 99)
                        mGlobalProgressBroadcastChannel.send(currentProgress.toInt())
                    else
                        mGlobalProgressBroadcastChannel.send(99)
                }

            }.addOnSuccessListener { taskSnapshot ->
                book.remoteFullBookUri = taskSnapshot.downloadUrl?.toString()
                saveBookOnDatabaseIfAllFilesUploaded(maxTaskCount, book, dataRepository)
            }.addOnFailureListener { exception ->
                launch {
                    val errorCode = (exception as StorageException).errorCode
                    when (errorCode) {
                        StorageException.ERROR_UNKNOWN -> mGlobalProgressBroadcastChannel.send(
                            errorCode
                        )
                        else -> mGlobalProgressBroadcastChannel.send(UPLOAD_STATUS_FAIL)
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
                                mGlobalProgressBroadcastChannel.send(currentProgress.toInt())
                            else
                                mGlobalProgressBroadcastChannel.send(99)

                        }
                    }.addOnSuccessListener { taskSnapshot ->
                        val uri = taskSnapshot.downloadUrl
                        uri?.let {
                            book.remoteChapterTitles.set(index, chapterTitle)
                            book.remoteChapterUris.set(index, it.toString())
                        }
                        saveBookOnDatabaseIfAllFilesUploaded(maxTaskCount, book, dataRepository)
                    }.addOnFailureListener { exception ->
                        launch {
                            val errorCode = (exception as StorageException).errorCode
                            when (errorCode) {
                                StorageException.ERROR_UNKNOWN -> mGlobalProgressBroadcastChannel.send(
                                    errorCode
                                )
                                else -> mGlobalProgressBroadcastChannel.send(UPLOAD_STATUS_FAIL)
                            }

                        }
                    }
                }
            }

            if (!editionFound){
                saveBookOnDatabaseIfAllFilesUploaded(1, book, dataRepository)
            }
        }

        return mGlobalProgressReceiver
    }

    private fun deleteBookFiles(filesToBeDeleted: ArraySet<String>){
        filesToBeDeleted.forEach {fileUri ->
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

        if (book.isLaunchedComplete){
            progressCap += 1
        } else {
            book.remoteChapterUris.forEach { uri ->
                if (!uri.isUriFromFirebaseStorage()){
                    progressCap +=1
                }
            }
        }

        return progressCap
    }

    private fun saveBookOnDatabaseIfAllFilesUploaded(maxTasksNumber: Int, book: Book, dataRepository: DataRepository) {
        mTasksReadyCounter++

        Log.i("FileRepository", "Tasks counter: $mTasksReadyCounter Max Tasks: $maxTasksNumber")
        if (mTasksReadyCounter == maxTasksNumber) {
            dataRepository.setBookDocuments(book)
            mTasksReadyCounter = 0
        }

    }

    fun getGlobalReceiver() = mGlobalProgressReceiver
}