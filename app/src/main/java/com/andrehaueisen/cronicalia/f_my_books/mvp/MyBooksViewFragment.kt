package com.andrehaueisen.cronicalia.f_my_books.mvp

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.andrehaueisen.cronicalia.R
import com.andrehaueisen.cronicalia.f_create_book.mvp.CreateBookActivity
import com.andrehaueisen.cronicalia.f_my_books.MyBooksAdapter
import com.andrehaueisen.cronicalia.models.Book
import com.andrehaueisen.cronicalia.models.User
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.channels.consumeEach
import kotlinx.coroutines.experimental.launch

/**
 * Created by andre on 2/19/2018.
 */
class MyBooksViewFragment : Fragment(), MyBooksAdapter.BookClickListener {

    interface BookClickListener {
        fun onBookClick(bookKey: String)
    }

    private lateinit var mMyCreationsRecyclerView: RecyclerView
    private lateinit var mUser: User

    companion object {

        fun newInstance(bundle: Bundle? = null): MyBooksViewFragment {

            val fragment = MyBooksViewFragment()
            bundle?.let {
                fragment.arguments = bundle
            }

            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        mUser = (activity as MyBooksPresenterActivity?)?.mUser!!

        val rootView = inflater.inflate(R.layout.f_fragment_my_books, container, false)

        mMyCreationsRecyclerView = rootView.findViewById(R.id.my_creations_recycler_view)

        initiateCreationsRecyclerView()
        initiateNewBookFAB(rootView)

        return rootView
    }

    private fun initiateCreationsRecyclerView() {

        mMyCreationsRecyclerView.setHasFixedSize(true)
        mMyCreationsRecyclerView.adapter = MyBooksAdapter(this, getListOfBooks())
        mMyCreationsRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

        launch(UI) {
            mUser.subscribeToUserUpdate().consumeEach {

                mMyCreationsRecyclerView.adapter?.let {
                    (mMyCreationsRecyclerView.adapter as MyBooksAdapter).updateData(getListOfBooks())
                }
            }
        }
    }

    private fun initiateNewBookFAB(rootView: View) {
        val createNewBookFAB = rootView.findViewById<FloatingActionButton>(R.id.create_book_fab)
        createNewBookFAB.setOnClickListener {
            val intent = Intent(context, CreateBookActivity::class.java)
            context!!.startActivity(intent)
        }
    }

    private fun getListOfBooks(): ArrayList<Book> {
        val listOfBooks = arrayListOf<Book>()
        mUser.books.values.forEach { book ->
            val newBook = book.copy()
            listOfBooks.add(newBook) }
        return listOfBooks
    }


    override fun onBookClick(bookKey: String) {
        try {
            (activity as? MyBooksPresenterActivity)?.onBookClick(bookKey)
        } catch (e: ClassCastException) {
            throw ClassCastException(activity.toString() + " must implement BookClickListener")
        }
    }
}