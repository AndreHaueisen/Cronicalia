package com.andrehaueisen.cronicalia.c_creations.mvp

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
import com.andrehaueisen.cronicalia.c_creations.MyCreationsAdapter
import com.andrehaueisen.cronicalia.d_create_book.mvp.CreateBookActivity
import com.andrehaueisen.cronicalia.models.Book
import com.andrehaueisen.cronicalia.models.User
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.channels.consumeEach
import kotlinx.coroutines.experimental.launch

/**
 * Created by andre on 2/19/2018.
 */
class MyCreationsViewFragment : Fragment(), MyCreationsAdapter.CreationClickListener {

    interface CreationClickListener {
        fun onCreationClick(bookKey: String)
    }

    private lateinit var mMyCreationsRecyclerView: RecyclerView
    private lateinit var mUser: User

    companion object {

        fun newInstance(bundle: Bundle? = null): MyCreationsViewFragment {

            val fragment = MyCreationsViewFragment()
            bundle?.let {
                fragment.arguments = bundle
            }

            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        mUser = (activity as MyCreationsPresenterActivity?)?.mUser!!

        val rootView = inflater.inflate(R.layout.c_fragment_my_creations, container, false)

        mMyCreationsRecyclerView = rootView.findViewById(R.id.my_creations_recycler_view)

        initiateCreationsRecyclerView()
        initiateNewBookFAB(rootView)

        return rootView
    }

    private fun initiateCreationsRecyclerView() {

        mMyCreationsRecyclerView.setHasFixedSize(true)
        mMyCreationsRecyclerView.adapter = MyCreationsAdapter(this, getListOfBooks())
        mMyCreationsRecyclerView.layoutManager = LinearLayoutManager(context)

        launch(UI) {
            mUser.subscribeToUserUpdate().consumeEach {

                mMyCreationsRecyclerView.adapter?.let {
                    (mMyCreationsRecyclerView.adapter as MyCreationsAdapter).updateData(getListOfBooks())
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
        mUser.books.values.forEach { book -> listOfBooks.add(book) }
        return listOfBooks
    }


    override fun onCreationClick(bookKey: String) {
        try {
            (activity as? MyCreationsPresenterActivity)?.onCreationClick(bookKey)
        } catch (e: ClassCastException) {
            throw ClassCastException(activity.toString() + " must implement CreationClickListener")
        }
    }
}