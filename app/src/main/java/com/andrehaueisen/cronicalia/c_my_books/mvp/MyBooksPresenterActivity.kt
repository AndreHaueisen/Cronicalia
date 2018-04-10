package com.andrehaueisen.cronicalia.c_my_books.mvp

import android.os.Bundle
import android.support.v4.util.ArraySet
import android.support.v7.app.AppCompatActivity
import com.andrehaueisen.cronicalia.BACK_STACK_CREATIONS_TO_EDIT_TAG
import com.andrehaueisen.cronicalia.FRAGMENT_EDIT_CREATION_TAG
import com.andrehaueisen.cronicalia.PARCELABLE_BOOK
import com.andrehaueisen.cronicalia.R
import com.andrehaueisen.cronicalia.a_application.BaseApplication
import com.andrehaueisen.cronicalia.c_my_books.dagger.DaggerMyBooksComponent
import com.andrehaueisen.cronicalia.c_my_books.dagger.MyBooksModule
import com.andrehaueisen.cronicalia.models.Book
import com.andrehaueisen.cronicalia.models.User
import com.andrehaueisen.cronicalia.utils.extensions.addFragment
import com.andrehaueisen.cronicalia.utils.extensions.getSmallestScreenWidth
import com.andrehaueisen.cronicalia.utils.extensions.replaceFragment
import kotlinx.coroutines.experimental.channels.SubscriptionReceiveChannel
import javax.inject.Inject


class MyBooksPresenterActivity : AppCompatActivity(), MyBooksViewFragment.CreationClickListener {

    interface PresenterActivity {
        fun refreshFragmentData(book: Book)
    }

    @Inject
    lateinit var mUser: User

    @Inject
    lateinit var mModel: MyBooksModel

    private val LOG_TAG = MyBooksPresenterActivity::class.java.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        DaggerMyBooksComponent.builder()
            .applicationComponent(BaseApplication.get(this).getAppComponent())
            .myBooksModule(MyBooksModule())
            .build()
            .inject(this)

        setContentView(R.layout.c_activity_my_books)

        if(savedInstanceState == null) {
            if (getSmallestScreenWidth() < 600) {
                val myCreationsViewFragment = MyBooksViewFragment.newInstance()
                addFragment(R.id.fragment_container, myCreationsViewFragment)
            } else {

                val bundle = Bundle()
                bundle.putParcelable(PARCELABLE_BOOK, mUser.books.values.first { book -> book.bookPosition == 0 })
                val editCreationFragment = MyBookEditViewFragment.newInstance(bundle)
                addFragment(R.id.edit_creation_fragment_container, editCreationFragment)
            }
        }

        /*upload_button.setOnClickListener {

            val userStorageReference = mStorageRef
                .child("brazil/ahb@gmail.com/the_black_river/chp_01/Home_theater_manual.pdf")
            val fileInputStream = assets.open("Home_theater_manual.pdf")

            val uploadTask = userStorageReference.putStream(fileInputStream)
            uploadTask.addOnSuccessListener {
                Log.i(LOG_TAG, "File uploaded!")
                Snackbar.make(fragment_container, "File uploaded!", Snackbar.LENGTH_INDEFINITE).show()
            }

            uploadTask.addOnFailureListener(object : OnFailureListener {
                override fun onFailure(exception: Exception) {
                    Log.e(LOG_TAG, exception.message)
                    Snackbar.make(
                        fragment_container,
                        "Upload failed!",
                        Snackbar.LENGTH_INDEFINITE
                    ).show()
                }
            })

        }

        download_display_button.setOnClickListener {
            val localFile = File(this.filesDir, "Home_theater_manual.pdf")
            val userStorageReference = mStorageRef
                .child("ahb@gmail.com/the_black_river/chp_01/Home_theater_manual.pdf")

            userStorageReference.getFile(localFile).addOnSuccessListener {
                val intent = Intent(this, PDFActivity::class.java)
                startActivity(intent)

            }.addOnFailureListener(object : OnFailureListener{
                    override fun onFailure(exception: Exception) {
                        Log.e(LOG_TAG, exception.message)
                        Snackbar.make(
                            fragment_container,
                            "Download failed!",
                            Snackbar.LENGTH_INDEFINITE
                        )
                    }
                })
        }*/
    }

    fun notifySimpleBookEdition(newTitle: String, collectionLocation: String, bookKey: String, variableToUpdate: MyBooksModel.SimpleUpdateVariable){
        mModel.simpleUpdateBook(newTitle, collectionLocation, bookKey, variableToUpdate)
    }

    suspend fun updateBookPoster(book: Book): SubscriptionReceiveChannel<Int?> {
        return mModel.updateBookPoster(book)
    }

    suspend fun updateBookCover(book: Book): SubscriptionReceiveChannel<Int?>{
        return mModel.updateBookCover(book)
    }

    suspend fun updateBookPdfs(book: Book, filesToBeDeleted: ArraySet<String>): SubscriptionReceiveChannel<Int?>{
        return mModel.updateBookPdfs(book, filesToBeDeleted)
    }

    fun updateBookPdfsReferences(book: Book){
        mModel.updateBookPdfsReferences(book)
    }

    fun deleteBook(book: Book){
        mModel.deleteBook(book)
    }

    override fun onCreationClick(bookKey: String) {

        if(getSmallestScreenWidth() >= 600){
            val editCreationFragment = (supportFragmentManager.findFragmentById(R.id.edit_creation_fragment_container) as? MyBookEditViewFragment)

            editCreationFragment?.let {
                if (editCreationFragment.isVisible)
                    editCreationFragment.refreshFragmentData(mUser.books[bookKey]!!)
            }

        } else {

            var editCreationFragment = (supportFragmentManager.findFragmentByTag(FRAGMENT_EDIT_CREATION_TAG) as? MyBookEditViewFragment)

            if (editCreationFragment == null) {
                val bundle = Bundle()
                bundle.putParcelable(PARCELABLE_BOOK, mUser.books[bookKey]!!)
                editCreationFragment = MyBookEditViewFragment.newInstance(bundle)
                replaceFragment(
                    containerId = R.id.fragment_container,
                    fragment = editCreationFragment,
                    fragmentTag = FRAGMENT_EDIT_CREATION_TAG,
                    stackTag = BACK_STACK_CREATIONS_TO_EDIT_TAG)

            } else {
                if (editCreationFragment.isVisible)
                    editCreationFragment.refreshFragmentData(mUser.books[bookKey]!!)
            }
        }

    }
}
