package com.andrehaueisen.cronicalia.c_featured_books.mvp

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.andrehaueisen.cronicalia.R
import com.andrehaueisen.cronicalia.c_featured_books.FeaturedBooksAdapter
import com.andrehaueisen.cronicalia.models.Book
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch

class FeaturedBooksFragment: Fragment() {

    companion object {

        fun newInstance(bundle: Bundle? = null): FeaturedBooksFragment {

            val fragment = FeaturedBooksFragment()
            bundle?.let {
                fragment.arguments = bundle
            }

            return fragment
        }
    }

    private lateinit var mFeaturedBooksRecyclerView : RecyclerView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.c_featured_books_fragment, container, false)

        mFeaturedBooksRecyclerView = view.findViewById(R.id.featured_books_recycler_view)
        initializeRecyclerView(mFeaturedBooksRecyclerView)


        return view
    }

    private fun initializeRecyclerView(recyclerView: RecyclerView){
        val activity = (requireActivity() as FeaturedBooksPresenterActivity)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.setHasFixedSize(true)

        with(activity) {
            launch(UI) {
                recyclerView.adapter = FeaturedBooksAdapter(
                    requireContext(),
                    getFeaturedBooksCollection(Book.BookGenre.ACTION),
                    getFeaturedBooksCollection(Book.BookGenre.ADVENTURE),
                    getFeaturedBooksCollection(Book.BookGenre.COMEDY),
                    getFeaturedBooksCollection(Book.BookGenre.DRAMA),
                    getFeaturedBooksCollection(Book.BookGenre.FANTASY),
                    getFeaturedBooksCollection(Book.BookGenre.FICTION),
                    getFeaturedBooksCollection(Book.BookGenre.HORROR),
                    getFeaturedBooksCollection(Book.BookGenre.MYTHOLOGY),
                    getFeaturedBooksCollection(Book.BookGenre.ROMANCE),
                    getFeaturedBooksCollection(Book.BookGenre.SATIRE)
                )
            }
        }
    }


}