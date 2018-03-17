package com.andrehaueisen.cronicalia.b_firebase

import android.net.Uri
import android.util.Log
import com.andrehaueisen.cronicalia.*
import com.andrehaueisen.cronicalia.models.Book
import com.andrehaueisen.cronicalia.models.User
import com.google.firebase.storage.StorageException
import com.google.firebase.storage.StorageMetadata
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.channels.ArrayBroadcastChannel
import kotlinx.coroutines.experimental.channels.SubscriptionReceiveChannel
import kotlinx.coroutines.experimental.launch


/**
 * Created by andre on 2/18/2018.
 */
class FileRepository(
    private val mStorageReference: StorageReference,
    private val mGlobalProgressBroadcastChannel: ArrayBroadcastChannel<Int?>,
    private val mGlobalProgressReceiver: SubscriptionReceiveChannel<Int?>,
    private val mUser: User
) {

    private var mTasksReadyCounter = 0

    suspend fun createBook(book: Book, uriTitleLinkedMap: LinkedHashMap<String, String>, dataRepository: DataRepository
    ): SubscriptionReceiveChannel<Int?> {

        var uploadTask: UploadTask
        val progressCap = getProgressCap(book, uriTitleLinkedMap)
        var currentProgress = 0.0

        val maxTaskCount = (progressCap).toInt()

        val locationReference = mStorageReference
            .child(book.getStorageRootLocation())
            .child(mUser.encodedEmail!!)
            .child(book.generateDocumentId())

        fun uploadPdfs() {

            if (book.isComplete) {
                val filePath = Uri.parse(book.localFullBookUri!!)
                uploadTask = locationReference.child("${book.title}.pdf".replace(" ", ""))
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
                uriTitleLinkedMap.keys.forEachIndexed { index, key ->
                    val filePath = Uri.parse(key)
                    val chapterNumberMetadata =
                        StorageMetadata.Builder().setCustomMetadata(METADATA_CHAPTER_NUMBER, index.toString()).build()
                    val chapterTitle = book.generateChapterRepositoryTitle(index, uriTitleLinkedMap[key]!!)
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
                            book.remoteMapChapterUriTitle.put(it.toString(), chapterTitle)
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

    private fun getProgressCap(book: Book, uriTitleLinkedMap: LinkedHashMap<String, String>): Double {
        var progressCap = 0.0

        book.localPosterUri?.let { progressCap += 1 }
        book.localCoverUri?.let { progressCap += 1 }
        if (book.isComplete)
            progressCap += 1
        else
            uriTitleLinkedMap.keys.forEach { progressCap += 1 }

        return progressCap
    }

    private fun saveBookOnDatabaseIfAllFilesUploaded(maxTasksNumber: Int, book: Book, dataRepository: DataRepository) {
        mTasksReadyCounter++

        Log.i("FileRepository", "Tasks counter: $mTasksReadyCounter Max Tasks: $maxTasksNumber")
        if (mTasksReadyCounter == maxTasksNumber) {
            dataRepository.createBook(book)
            mTasksReadyCounter = 0
        }

    }

    fun getGlobalReceiver() = mGlobalProgressReceiver
}