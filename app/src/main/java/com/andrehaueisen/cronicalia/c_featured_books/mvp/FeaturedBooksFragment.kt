package com.andrehaueisen.cronicalia.c_featured_books.mvp

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.andrehaueisen.cronicalia.R
import com.andrehaueisen.cronicalia.c_featured_books.BookAdapter
import com.andrehaueisen.cronicalia.c_featured_books.FeaturedBooksAdapter
import com.andrehaueisen.cronicalia.models.Book

class FeaturedBooksFragment : Fragment(), BookAdapter.BookClickListener {

    companion object {

        fun newInstance(bundle: Bundle? = null): FeaturedBooksFragment {

            val fragment = FeaturedBooksFragment()
            bundle?.let {
                fragment.arguments = bundle
            }
            return fragment
        }
    }

    interface BookClickListener {
        fun onBookClick(bookGenre: Book.BookGenre, bookKey: String)
    }

    private lateinit var mFeaturedBooksRecyclerView: RecyclerView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.c_fragment_featured_books, container, false)

        mFeaturedBooksRecyclerView = view.findViewById(R.id.featured_books_recycler_view)

        initializeRecyclerView(mFeaturedBooksRecyclerView)

        return view
    }


    private fun initializeRecyclerView(recyclerView: RecyclerView) {
        val activity = (requireActivity() as FeaturedBooksPresenterActivity)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.setHasFixedSize(true)

        with(activity) {

            recyclerView.adapter = FeaturedBooksAdapter(
                this@FeaturedBooksFragment,
                getActionBooks(),
                getAdventureBooks(),
                getComedyBooks(),
                getDramaBooks(),
                getFantasyBooks(),
                getFictionBooks(),
                getHorrorBooks(),
                getMythologyBooks(),
                getRomanceBooks(),
                getSatireBooks()
            )

        }
    }

    fun notifyBooksListReady(layoutPosition: Int){
        (mFeaturedBooksRecyclerView.adapter as FeaturedBooksAdapter).notifyBooksListReady(layoutPosition)
    }

    override fun onBookClick(bookGenre: Book.BookGenre, bookKey: String) {
        val activity = (activity as? FeaturedBooksPresenterActivity)

        if (activity != null && isAdded) {
            activity.onBookClick(bookGenre, bookKey)
        }
    }
}