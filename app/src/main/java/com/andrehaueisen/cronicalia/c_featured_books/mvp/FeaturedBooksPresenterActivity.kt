package com.andrehaueisen.cronicalia.c_featured_books.mvp

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.andrehaueisen.cronicalia.R
import com.andrehaueisen.cronicalia.b_firebase.DataRepository
import com.andrehaueisen.cronicalia.f_my_books.mvp.MyBooksPresenterActivity
import com.andrehaueisen.cronicalia.g_manage_account.mvp.ManageAccountPresenterActivity
import com.andrehaueisen.cronicalia.models.Book
import com.andrehaueisen.cronicalia.utils.extensions.addFragment
import com.andrehaueisen.cronicalia.utils.extensions.getSmallestScreenWidth
import com.andrehaueisen.cronicalia.utils.extensions.startNewActivity
import kotlinx.android.synthetic.main.c_featured_books_activity.*
import org.koin.android.ext.android.inject

class FeaturedBooksPresenterActivity: AppCompatActivity() {

    private lateinit var mModel: FeaturedBooksModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.c_featured_books_activity)

        val dataRepository: DataRepository by inject()
        mModel = FeaturedBooksModel(dataRepository)

        if(savedInstanceState == null) {
            if (getSmallestScreenWidth() < 600) {
                val myCreationsViewFragment = FeaturedBooksFragment.newInstance()
                addFragment(R.id.fragment_container, myCreationsViewFragment)

            } else {

                /*val bundle = Bundle()
                bundle.putParcelable(PARCELABLE_BOOK, mUser.books.values.first { book -> book.bookPosition == 0 })
                val editCreationFragment = MyBookEditViewFragment.newInstance(bundle)
                addFragment(R.id.edit_creation_fragment_container, editCreationFragment)*/
            }
        }

        navigation_bottom_view.setOnNavigationItemSelectedListener { menuItem ->

            when(menuItem.itemId){
                R.id.action_featured -> {}
                R.id.action_search -> {}
                R.id.action_reading_collection -> {}
                R.id.action_my_books -> {
                    startNewActivity(MyBooksPresenterActivity::class.java, listOf(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT))
                }
                R.id.action_account -> {
                    startNewActivity(ManageAccountPresenterActivity::class.java, listOf(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT))
                }
            }

            false
        }

        val menuItem = navigation_bottom_view.menu.getItem(0)
        menuItem.isChecked = true

    }

    suspend fun getFeaturedBooksCollection(queryingGenre: Book.BookGenre): List<Book>{
        return mModel.getFeaturedBooks(queryingGenre)
    }
}