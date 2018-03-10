package com.andrehaueisen.cronicalia.b_firebase

import android.net.Uri
import android.util.Log
import com.andrehaueisen.cronicalia.METADATA_PROPERTY_IMAGE_TYPE_COVER
import com.andrehaueisen.cronicalia.METADATA_PROPERTY_IMAGE_TYPE_POSTER
import com.andrehaueisen.cronicalia.METADATA_TITLE_IMAGE_TYPE
import com.andrehaueisen.cronicalia.models.Book
import com.andrehaueisen.cronicalia.models.User
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
class FileRepository(private val mStorageReference: StorageReference,
                     private val mGlobalProgressBroadcastChannel: ArrayBroadcastChannel<Double?>,
                     private val mGlobalProgressReceiver: SubscriptionReceiveChannel<Double?>,
                     private val mUser: User) {

    private var mTasksReadyCounter = 0

    suspend fun createBook(book: Book, dataRepository: DataRepository): SubscriptionReceiveChannel<Double?> {
        var uploadTask: UploadTask

        val progressCap = getProgressCap(book) + 1 //TODO review +1 bug
        var currentProgress = 0.0

        val maxTaskCount = (progressCap-1).toInt()

        val locationReference = mStorageReference
            .child(book.getStorageRootLocation())
            .child(mUser.encodedEmail)
            .child(book.generateDocumentId()!!)

        fun uploadPdfs() {

            if (book.isComplete) {
                val filePath = Uri.parse(book.localFullBookUri!!)
                uploadTask = locationReference.child("${book.title}.pdf".replace(" ", "")).putFile(filePath)
                uploadTask.addOnProgressListener { taskSnapshot ->
                        launch {
                            currentProgress += (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount) / progressCap
                            mGlobalProgressBroadcastChannel.send(currentProgress)
                        }

                }.addOnSuccessListener { taskSnapshot ->
                    book.remoteFullBookUri = taskSnapshot.downloadUrl?.toString()
                    saveBookOnDatabaseIfAllFilesUploaded(maxTaskCount, book, dataRepository)
                }

            } else {
                book.localMapChapterUriTitle.keys.forEachIndexed  {index, key ->
                    val filePath = Uri.parse(key)
                    val chapterTitle = book.generateFileRepositoryTitle( index, key)
                    uploadTask = locationReference.child("$chapterTitle.pdf").putFile(filePath)
                    uploadTask.addOnProgressListener { taskSnapshot ->
                        launch(UI) {
                            currentProgress += (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount) / progressCap
                            mGlobalProgressBroadcastChannel.send(currentProgress)

                        }
                    }.addOnSuccessListener { taskSnapshot ->
                        val uri = taskSnapshot.downloadUrl
                        uri?.let{
                            book.remoteMapChapterUriTitle.put(it.toString(), chapterTitle)
                        }
                        saveBookOnDatabaseIfAllFilesUploaded(maxTaskCount, book, dataRepository)
                    }
                }

            }
        }

        fun uploadImages(){

            book.localCoverUri?.let { coverUri ->
                val metadata = StorageMetadata.Builder()
                    .setCustomMetadata(METADATA_TITLE_IMAGE_TYPE, METADATA_PROPERTY_IMAGE_TYPE_COVER)
                    .build()

                val filePath = Uri.parse(coverUri)
                uploadTask = locationReference.child(filePath.lastPathSegment).putFile(filePath, metadata)
                uploadTask.addOnProgressListener { taskSnapshot ->
                    launch(UI) {
                        currentProgress += (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount) / progressCap
                        mGlobalProgressBroadcastChannel.send(currentProgress)
                    }
                }.addOnSuccessListener { taskSnapshot ->
                    book.remoteCoverUri = taskSnapshot.downloadUrl?.toString()
                    saveBookOnDatabaseIfAllFilesUploaded(maxTaskCount, book, dataRepository)
                }
            }

            book.localPosterUri?.let { posterUri ->
                val metadata = StorageMetadata.Builder()
                    .setCustomMetadata(METADATA_TITLE_IMAGE_TYPE, METADATA_PROPERTY_IMAGE_TYPE_POSTER)
                    .build()

                val filePath = Uri.parse(posterUri)
                uploadTask = locationReference.child(filePath.lastPathSegment).putFile(filePath, metadata)
                uploadTask.addOnProgressListener { taskSnapshot ->
                    launch(UI) {
                        currentProgress += (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount) / progressCap
                        mGlobalProgressBroadcastChannel.send(currentProgress)
                    }
                }.addOnSuccessListener { taskSnapshot ->
                    book.remotePosterUri = taskSnapshot.downloadUrl?.toString()
                    saveBookOnDatabaseIfAllFilesUploaded(maxTaskCount, book, dataRepository)
                }
            }
        }

        uploadPdfs()
        uploadImages()

        return mGlobalProgressReceiver
    }

    private fun getProgressCap(book: Book) : Double{
        var progressCap = 0.0

        book.localPosterUri?.let { progressCap += 1 }
        book.localCoverUri?.let { progressCap += 1 }
        if (book.isComplete)
            progressCap += 1
        else
            book.localMapChapterUriTitle.keys.forEach { progressCap += 1 }

        return progressCap
    }

    private fun saveBookOnDatabaseIfAllFilesUploaded(maxTasksNumber: Int, book: Book, dataRepository: DataRepository){
        mTasksReadyCounter++

        Log.i("FileRepository", "Tasks counter: $mTasksReadyCounter Max Tasks: $maxTasksNumber")
        if(mTasksReadyCounter == maxTasksNumber){
            dataRepository.createBook(book)
            mTasksReadyCounter = 0
        }

    }

    fun getGlobalReceiver() = mGlobalProgressReceiver
}