package com.andrehaueisen.cronicalia.c_creations.mvp

import android.content.Context
import android.content.Intent
import android.support.design.widget.FloatingActionButton
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import com.andrehaueisen.cronicalia.R
import com.andrehaueisen.cronicalia.c_creations.MyCreationsAdapter
import com.andrehaueisen.cronicalia.d_create_book.mvp.CreateBookActivity
import com.andrehaueisen.cronicalia.models.User

/**
 * Created by andre on 2/19/2018.
 */
class MyCreationsView(private val mContext: Context, private val mRootView: View, private val user: User) {

    init {
        initiateCreationsRecyclerView()
        initiateNewBookFAB()
    }

    private fun initiateCreationsRecyclerView(){
        val myCreationsRecyclerView = mRootView.findViewById<RecyclerView>(R.id.my_creations_recycler_view)
        myCreationsRecyclerView.setHasFixedSize(true)
        myCreationsRecyclerView.adapter = MyCreationsAdapter(mContext, user.books.values.toList())
        myCreationsRecyclerView.layoutManager = LinearLayoutManager(mContext)
    }

    private fun initiateNewBookFAB(){
        val createNewBookFAB = mRootView.findViewById<FloatingActionButton>(R.id.create_book_fab)
        createNewBookFAB.setOnClickListener {
            val intent = Intent(mContext, CreateBookActivity::class.java)
            mContext.startActivity(intent)
        }
    }
}