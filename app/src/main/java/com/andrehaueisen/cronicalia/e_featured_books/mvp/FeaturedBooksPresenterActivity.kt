package com.andrehaueisen.cronicalia.e_featured_books.mvp

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.andrehaueisen.cronicalia.R
import com.andrehaueisen.cronicalia.c_my_books.mvp.MyBooksPresenterActivity
import com.andrehaueisen.cronicalia.utils.extensions.startNewActivity
import kotlinx.android.synthetic.main.e_featured_books_activity.*

class FeaturedBooksPresenterActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.e_featured_books_activity)

        navigation_bottom_view.setOnNavigationItemSelectedListener { menuItem ->

            when(menuItem.itemId){
                R.id.action_starred -> {}
                R.id.action_search -> {}
                R.id.action_reading_collection -> {}
                R.id.action_my_books -> {
                    startNewActivity(MyBooksPresenterActivity::class.java, listOf(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT))
                }
            }

            false
        }

        val menuItem = navigation_bottom_view.menu.getItem(0)
        menuItem.isChecked = true

    }
}