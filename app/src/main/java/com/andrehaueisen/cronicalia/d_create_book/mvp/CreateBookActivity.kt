package com.andrehaueisen.cronicalia.d_create_book.mvp

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.View
import com.andrehaueisen.cronicalia.R


class CreateBookActivity : AppCompatActivity() {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setSupportActionBar(findViewById<Toolbar>(R.id.create_book_app_bar))
        actionBar?.let {
            title = getString(R.string.create_new_book)
        }

        setContentView(R.layout.d_activity_create_book)

        CreateBookView(this, findViewById<View>(R.id.create_book_root_view))
    }


}
