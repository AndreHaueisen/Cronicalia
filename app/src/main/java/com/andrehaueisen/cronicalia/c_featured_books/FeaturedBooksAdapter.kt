package com.andrehaueisen.cronicalia.c_featured_books

import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.andrehaueisen.cronicalia.R
import com.andrehaueisen.cronicalia.c_featured_books.mvp.FeaturedBooksFragment
import com.andrehaueisen.cronicalia.models.Book

class FeaturedBooksAdapter(
    val fragment: FeaturedBooksFragment,
    val actionBookCollection: List<Book>,
    val adventureBookCollection: List<Book>,
    val comedyBookCollection: List<Book>,
    val dramaBookCollection: List<Book>,
    val fantasyBookCollection: List<Book>,
    val fictionBookCollection: List<Book>,
    val horrorBookCollection: List<Book>,
    val mythologyBookCollection: List<Book>,
    val romanceBookCollection: List<Book>,
    val satireBookCollection: List<Book>
) : RecyclerView.Adapter<FeaturedBooksAdapter.FeaturedBooksHolder>() {

    val alreadyLoadedCollectionSet = mutableSetOf<Book.BookGenre>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeaturedBooksHolder {

        val view = LayoutInflater.from(fragment.requireContext()).inflate(R.layout.item_collection_featured_books, parent, false)

        return FeaturedBooksHolder(view)
    }

    override fun getItemCount(): Int {
        var itemCount = 0
        if (actionBookCollection.isNotEmpty()) itemCount++
        if (adventureBookCollection.isNotEmpty()) itemCount++
        if (comedyBookCollection.isNotEmpty()) itemCount++
        if (dramaBookCollection.isNotEmpty()) itemCount++
        if (fantasyBookCollection.isNotEmpty()) itemCount++
        if (fictionBookCollection.isNotEmpty()) itemCount++
        if (horrorBookCollection.isNotEmpty()) itemCount++
        if (mythologyBookCollection.isNotEmpty()) itemCount++
        if (romanceBookCollection.isNotEmpty()) itemCount++
        if (satireBookCollection.isNotEmpty()) itemCount++

        return itemCount
    }

    fun notifyBooksListReady(layoutPosition: Int){
        notifyItemInserted(layoutPosition)
    }


    override fun onBindViewHolder(holder: FeaturedBooksHolder, position: Int) {
        holder.bindCollectionsToView()
    }

    inner class FeaturedBooksHolder(booksView: View) : RecyclerView.ViewHolder(booksView) {

        private val mGenreTextView = booksView.findViewById<TextView>(R.id.collection_genre_text_view)
        private val mGenreCollectionRecyclerView = booksView.findViewById<RecyclerView>(R.id.books_collection_recycler_view)

        internal fun bindCollectionsToView() {

            mGenreCollectionRecyclerView.layoutManager = LinearLayoutManager(fragment.requireContext(), LinearLayoutManager.HORIZONTAL, false)
            mGenreCollectionRecyclerView.setHasFixedSize(true)

            if (actionBookCollection.isNotEmpty() && !alreadyLoadedCollectionSet.contains(Book.BookGenre.ACTION)) {
                mGenreTextView.text = Book.BookGenre.ACTION.toString()
                mGenreCollectionRecyclerView.adapter = BookAdapter(fragment, actionBookCollection)
                alreadyLoadedCollectionSet.add(Book.BookGenre.ACTION)
                return
            }

            if (adventureBookCollection.isNotEmpty() && !alreadyLoadedCollectionSet.contains(Book.BookGenre.ADVENTURE)) {
                mGenreTextView.text = Book.BookGenre.ADVENTURE.toString()
                mGenreCollectionRecyclerView.adapter = BookAdapter(fragment, adventureBookCollection)
                alreadyLoadedCollectionSet.add(Book.BookGenre.ADVENTURE)
                return
            }

            if (comedyBookCollection.isNotEmpty() && !alreadyLoadedCollectionSet.contains(Book.BookGenre.COMEDY)) {
                mGenreTextView.text = Book.BookGenre.COMEDY.toString()
                mGenreCollectionRecyclerView.adapter = BookAdapter(fragment, comedyBookCollection)
                alreadyLoadedCollectionSet.add(Book.BookGenre.COMEDY)
                return
            }

            if (dramaBookCollection.isNotEmpty() && !alreadyLoadedCollectionSet.contains(Book.BookGenre.DRAMA)) {
                mGenreTextView.text = Book.BookGenre.DRAMA.toString()
                mGenreCollectionRecyclerView.adapter = BookAdapter(fragment, dramaBookCollection)
                alreadyLoadedCollectionSet.add(Book.BookGenre.DRAMA)
                return
            }

            if (fantasyBookCollection.isNotEmpty() && !alreadyLoadedCollectionSet.contains(Book.BookGenre.FANTASY)) {
                mGenreTextView.text = Book.BookGenre.FANTASY.toString()
                mGenreCollectionRecyclerView.adapter = BookAdapter(fragment, fantasyBookCollection)
                alreadyLoadedCollectionSet.add(Book.BookGenre.FANTASY)
                return
            }

            if (fictionBookCollection.isNotEmpty() && !alreadyLoadedCollectionSet.contains(Book.BookGenre.FICTION)) {
                mGenreTextView.text = Book.BookGenre.FICTION.toString()
                mGenreCollectionRecyclerView.adapter = BookAdapter(fragment, fictionBookCollection)
                alreadyLoadedCollectionSet.add(Book.BookGenre.FICTION)
                return
            }

            if (horrorBookCollection.isNotEmpty() && !alreadyLoadedCollectionSet.contains(Book.BookGenre.HORROR)) {
                mGenreTextView.text = Book.BookGenre.HORROR.toString()
                mGenreCollectionRecyclerView.adapter = BookAdapter(fragment, horrorBookCollection)
                alreadyLoadedCollectionSet.add(Book.BookGenre.HORROR)
                return
            }

            if (mythologyBookCollection.isNotEmpty() && !alreadyLoadedCollectionSet.contains(Book.BookGenre.MYTHOLOGY)) {
                mGenreTextView.text = Book.BookGenre.MYTHOLOGY.toString()
                mGenreCollectionRecyclerView.adapter = BookAdapter(fragment, mythologyBookCollection)
                alreadyLoadedCollectionSet.add(Book.BookGenre.MYTHOLOGY)
                return
            }

            if (romanceBookCollection.isNotEmpty() && !alreadyLoadedCollectionSet.contains(Book.BookGenre.ROMANCE)) {
                mGenreTextView.text = Book.BookGenre.ROMANCE.toString()
                mGenreCollectionRecyclerView.adapter = BookAdapter(fragment, romanceBookCollection)
                alreadyLoadedCollectionSet.add(Book.BookGenre.ROMANCE)
                return
            }

            if (satireBookCollection.isNotEmpty() && !alreadyLoadedCollectionSet.contains(Book.BookGenre.SATIRE)) {
                mGenreTextView.text = Book.BookGenre.SATIRE.toString()
                mGenreCollectionRecyclerView.adapter = BookAdapter(fragment, satireBookCollection)
                alreadyLoadedCollectionSet.add(Book.BookGenre.SATIRE)
                return
            }
        }
    }

}