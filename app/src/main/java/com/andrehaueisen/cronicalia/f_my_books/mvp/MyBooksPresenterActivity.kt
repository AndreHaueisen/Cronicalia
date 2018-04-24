package com.andrehaueisen.cronicalia.f_my_books.mvp

import android.content.Intent
import android.os.Bundle
import android.support.v4.util.ArraySet
import android.support.v7.app.AppCompatActivity
import com.andrehaueisen.cronicalia.BACK_STACK_CREATIONS_TO_EDIT_TAG
import com.andrehaueisen.cronicalia.FRAGMENT_EDIT_CREATION_TAG
import com.andrehaueisen.cronicalia.PARCELABLE_BOOK
import com.andrehaueisen.cronicalia.R
import com.andrehaueisen.cronicalia.b_firebase.DataRepository
import com.andrehaueisen.cronicalia.b_firebase.FileRepository
import com.andrehaueisen.cronicalia.c_featured_books.mvp.FeaturedBooksPresenterActivity
import com.andrehaueisen.cronicalia.g_manage_account.mvp.ManageAccountPresenterActivity
import com.andrehaueisen.cronicalia.models.Book
import com.andrehaueisen.cronicalia.models.User
import com.andrehaueisen.cronicalia.utils.extensions.addFragment
import com.andrehaueisen.cronicalia.utils.extensions.getSmallestScreenWidth
import com.andrehaueisen.cronicalia.utils.extensions.replaceFragment
import com.andrehaueisen.cronicalia.utils.extensions.startNewActivity
import kotlinx.android.synthetic.main.f_activity_my_books.*
import kotlinx.coroutines.experimental.channels.SubscriptionReceiveChannel
import org.koin.android.ext.android.inject


class MyBooksPresenterActivity : AppCompatActivity(), MyBooksViewFragment.CreationClickListener {

    interface PresenterActivity {
        fun refreshFragmentData(book: Book)
    }

    private lateinit var mModel: MyBooksModel
    val mUser: User by inject()

    private val LOG_TAG = MyBooksPresenterActivity::class.java.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.f_activity_my_books)

        val fileRepository : FileRepository by inject()
        val dataRepository : DataRepository by inject()
        mModel = MyBooksModel(fileRepository, dataRepository)

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

        navigation_bottom_view.setOnNavigationItemSelectedListener { menuItem ->

            when(menuItem.itemId){
                R.id.action_featured -> {
                    startNewActivity(FeaturedBooksPresenterActivity::class.java, listOf(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT))
                }
                R.id.action_search -> {}
                R.id.action_reading_collection -> {}
                R.id.action_my_books -> {}
                R.id.action_account -> {
                    startNewActivity(ManageAccountPresenterActivity::class.java, listOf(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT))
                }
            }

            false
        }

        val menuItem = navigation_bottom_view.menu.getItem(3)
        menuItem.isChecked = true

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

    suspend fun updateBookPoster(book: Book): Int {
        return mModel.updateBookPoster(book)
    }

    suspend fun updateBookCover(book: Book): Int{
        return mModel.updateBookCover(book)
    }

    suspend fun updateBookPdfs(book: Book, filesToBeDeleted: ArraySet<String>): SubscriptionReceiveChannel<Int>{
        return mModel.updateBookPdfs(book, filesToBeDeleted)
    }

    suspend fun updateBookPdfsReferences(book: Book): Int{
        return mModel.updateBookPdfsReferences(book)
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
