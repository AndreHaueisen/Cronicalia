package com.andrehaueisen.cronicalia.c_featured_books.mvp

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.andrehaueisen.cronicalia.*
import com.andrehaueisen.cronicalia.b_firebase.DataRepository
import com.andrehaueisen.cronicalia.f_my_books.mvp.MyBooksPresenterActivity
import com.andrehaueisen.cronicalia.g_manage_account.mvp.ManageAccountPresenterActivity
import com.andrehaueisen.cronicalia.models.Book
import com.andrehaueisen.cronicalia.utils.extensions.addFragment
import com.andrehaueisen.cronicalia.utils.extensions.getSmallestScreenWidth
import com.andrehaueisen.cronicalia.utils.extensions.replaceFragment
import com.andrehaueisen.cronicalia.utils.extensions.startNewActivity
import kotlinx.android.synthetic.main.c_activity_featured_books.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import org.koin.android.ext.android.inject

class FeaturedBooksPresenterActivity : AppCompatActivity(), FeaturedBooksFragment.BookClickListener {

    private var mActionBooks = ArrayList<Book>()
    private var mAdventureBooks = ArrayList<Book>()
    private var mComedyBooks = ArrayList<Book>()
    private var mDramaBooks = ArrayList<Book>()
    private var mFantasyBooks = ArrayList<Book>()
    private var mFictionBooks = ArrayList<Book>()
    private var mHorrorBooks = ArrayList<Book>()
    private var mMythologyBooks = ArrayList<Book>()
    private var mRomanceBooks = ArrayList<Book>()
    private var mSatireBooks = ArrayList<Book>()
    private lateinit var mModel: FeaturedBooksModel
    private var mPreviousBookKey = ""

    override fun onSaveInstanceState(outState: Bundle) {

        outState.putParcelableArrayList(PARCELABLE_ACTION_BOOK_LIST, mActionBooks)
        outState.putParcelableArrayList(PARCELABLE_ADVENTURE_BOOK_LIST, mAdventureBooks)
        outState.putParcelableArrayList(PARCELABLE_COMEDY_BOOK_LIST, mComedyBooks)
        outState.putParcelableArrayList(PARCELABLE_DRAMA_BOOK_LIST, mDramaBooks)
        outState.putParcelableArrayList(PARCELABLE_FANTASY_BOOK_LIST, mFantasyBooks)
        outState.putParcelableArrayList(PARCELABLE_FICTION_BOOK_LIST, mFictionBooks)
        outState.putParcelableArrayList(PARCELABLE_HORROR_BOOK_LIST, mHorrorBooks)
        outState.putParcelableArrayList(PARCELABLE_MYTHOLOGY_BOOK_LIST, mMythologyBooks)
        outState.putParcelableArrayList(PARCELABLE_ROMANCE_BOOK_LIST, mRomanceBooks)
        outState.putParcelableArrayList(PARCELABLE_SATIRE_BOOK_LIST, mSatireBooks)

        super.onSaveInstanceState(outState)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.c_activity_featured_books)

        val dataRepository: DataRepository by inject()
        mModel = FeaturedBooksModel(dataRepository)

        if (savedInstanceState == null && getSmallestScreenWidth() < 600) {

            val myCreationsViewFragment = FeaturedBooksFragment.newInstance()
            addFragment(R.id.fragment_container, myCreationsViewFragment, tag = FRAGMENT_FEATURED_BOOKS_TAG)
        }

        if (savedInstanceState != null) {
            mActionBooks = savedInstanceState.getParcelableArrayList(PARCELABLE_ACTION_BOOK_LIST)
            mAdventureBooks = savedInstanceState.getParcelableArrayList(PARCELABLE_ADVENTURE_BOOK_LIST)
            mComedyBooks = savedInstanceState.getParcelableArrayList(PARCELABLE_COMEDY_BOOK_LIST)
            mDramaBooks = savedInstanceState.getParcelableArrayList(PARCELABLE_DRAMA_BOOK_LIST)
            mFantasyBooks = savedInstanceState.getParcelableArrayList(PARCELABLE_FANTASY_BOOK_LIST)
            mFictionBooks = savedInstanceState.getParcelableArrayList(PARCELABLE_FICTION_BOOK_LIST)
            mHorrorBooks = savedInstanceState.getParcelableArrayList(PARCELABLE_HORROR_BOOK_LIST)
            mMythologyBooks = savedInstanceState.getParcelableArrayList(PARCELABLE_MYTHOLOGY_BOOK_LIST)
            mRomanceBooks = savedInstanceState.getParcelableArrayList(PARCELABLE_ROMANCE_BOOK_LIST)
            mSatireBooks = savedInstanceState.getParcelableArrayList(PARCELABLE_SATIRE_BOOK_LIST)
        } else {
            launch { getFeaturedBooksCollection() }
        }

        navigation_bottom_view.setOnNavigationItemSelectedListener { menuItem ->

            when (menuItem.itemId) {
                R.id.action_featured -> {
                }
                R.id.action_search -> {
                }
                R.id.action_reading_collection -> {
                }
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

    private suspend fun getFeaturedBooksCollection() {

        var layoutPosition = 0
        launch(UI) {

            Book.BookGenre.values().forEach { bookGenre ->
                when (bookGenre) {

                    Book.BookGenre.ACTION -> {
                        if(getBooksAndNotifyWhenReady(mActionBooks, bookGenre, layoutPosition)) layoutPosition++

                        //shows the first book without click
                        if (getSmallestScreenWidth() >= 600) {

                            val firstBook = mActionBooks[0]
                            val firstBookOpinions = mModel.fetchOpinions(firstBook.generateBookKey()).await()

                            val bundle = Bundle()
                            bundle.putParcelable(PARCELABLE_BOOK, firstBook)
                            bundle.putParcelableArrayList(PARCELABLE_BOOK_OPINIONS, firstBookOpinions)
                            val selectedBookFragment = SelectedBookFragment.newInstance(bundle)
                            addFragment(R.id.selected_book_fragment_container, selectedBookFragment)

                        }
                    }

                    Book.BookGenre.ADVENTURE -> {
                        if(getBooksAndNotifyWhenReady(mAdventureBooks, bookGenre, layoutPosition)) layoutPosition++
                    }
                    Book.BookGenre.COMEDY -> {
                        if(getBooksAndNotifyWhenReady(mComedyBooks, bookGenre, layoutPosition)) layoutPosition++
                    }
                    Book.BookGenre.DRAMA -> {
                        if(getBooksAndNotifyWhenReady(mDramaBooks, bookGenre, layoutPosition)) layoutPosition++
                    }
                    Book.BookGenre.FANTASY -> {
                        if(getBooksAndNotifyWhenReady(mFantasyBooks, bookGenre, layoutPosition)) layoutPosition++
                    }
                    Book.BookGenre.FICTION -> {
                        if(getBooksAndNotifyWhenReady(mFictionBooks, bookGenre, layoutPosition)) layoutPosition++
                    }
                    Book.BookGenre.HORROR -> {
                        if(getBooksAndNotifyWhenReady(mHorrorBooks, bookGenre, layoutPosition)) layoutPosition++
                    }
                    Book.BookGenre.MYTHOLOGY -> {
                        if(getBooksAndNotifyWhenReady(mMythologyBooks, bookGenre, layoutPosition)) layoutPosition++
                    }
                    Book.BookGenre.ROMANCE -> {
                        if(getBooksAndNotifyWhenReady(mRomanceBooks, bookGenre, layoutPosition)) layoutPosition++
                    }
                    Book.BookGenre.SATIRE -> {
                        if(getBooksAndNotifyWhenReady(mSatireBooks, bookGenre, layoutPosition)) layoutPosition++
                    }
                    Book.BookGenre.UNDEFINED -> {
                        return@forEach
                    }

                }
            }
        }

    }

    /**
     * Requests book list from database and notify the fragment if the list returned is not empty.
     * Returns true if the list is not empty to signal layout position increment
     **/

    private suspend fun getBooksAndNotifyWhenReady(booksList: ArrayList<Book>, bookGenre: Book.BookGenre, layoutPosition: Int): Boolean{
        booksList.addAll(mModel.getFeaturedBooks(bookGenre).await())
        val featuredBooksFragment = (supportFragmentManager.findFragmentById(R.id.fragment_container) as? FeaturedBooksFragment)
        if(booksList.isNotEmpty()){
            featuredBooksFragment?.notifyBooksListReady(layoutPosition)
        }

        return booksList.isNotEmpty()
    }

    override fun onBookClick(bookGenre: Book.BookGenre, bookKey: String) {

        val selectedBook = when(bookGenre){
            Book.BookGenre.ACTION -> mActionBooks.find { book -> book.generateBookKey() == bookKey }
            Book.BookGenre.ADVENTURE -> mAdventureBooks.find { book -> book.generateBookKey() == bookKey }
            Book.BookGenre.COMEDY -> mComedyBooks.find { book -> book.generateBookKey() == bookKey }
            Book.BookGenre.DRAMA -> mDramaBooks.find { book -> book.generateBookKey() == bookKey }
            Book.BookGenre.FANTASY -> mFantasyBooks.find { book -> book.generateBookKey() == bookKey }
            Book.BookGenre.FICTION -> mFictionBooks.find { book -> book.generateBookKey() == bookKey }
            Book.BookGenre.HORROR -> mHorrorBooks.find { book -> book.generateBookKey() == bookKey }
            Book.BookGenre.MYTHOLOGY -> mMythologyBooks.find { book -> book.generateBookKey() == bookKey }
            Book.BookGenre.ROMANCE -> mRomanceBooks.find { book -> book.generateBookKey() == bookKey }
            Book.BookGenre.SATIRE -> mSatireBooks.find { book -> book.generateBookKey() == bookKey }
            Book.BookGenre.UNDEFINED -> mActionBooks.find { book -> book.generateBookKey() == bookKey }
        }

        launch(UI) {

            if (getSmallestScreenWidth() >= 600 && bookKey != mPreviousBookKey) {

                val bookOpinions = mModel.fetchOpinions(bookKey).await()

                val selectedBookFragment =
                    (supportFragmentManager.findFragmentById(R.id.selected_book_fragment_container) as? SelectedBookFragment)

                selectedBookFragment?.let {
                    if (selectedBookFragment.isVisible)
                        selectedBookFragment.refreshFragmentData(selectedBook!!, bookOpinions)
                }
                mPreviousBookKey = bookKey

            } else if (getSmallestScreenWidth() < 600) {

                val bookOpinions = mModel.fetchOpinions(bookKey).await()

                var selectedBookFragment =
                    (supportFragmentManager.findFragmentByTag(FRAGMENT_BOOK_SELECTED_TAG) as? SelectedBookFragment)

                if (selectedBookFragment == null) {
                    val bundle = Bundle()
                    bundle.putParcelable(PARCELABLE_BOOK, selectedBook!!)
                    bundle.putParcelableArrayList(PARCELABLE_BOOK_OPINIONS, bookOpinions)
                    selectedBookFragment = SelectedBookFragment.newInstance(bundle)
                    replaceFragment(
                        containerId = R.id.fragment_container,
                        fragment = selectedBookFragment,
                        fragmentTag = FRAGMENT_BOOK_SELECTED_TAG,
                        stackTag = BACK_STACK_FEATURED_TO_SELECTED_TAG
                    )

                } else {
                    replaceFragment(
                        containerId = R.id.fragment_container,
                        fragment = selectedBookFragment,
                        fragmentTag = FRAGMENT_BOOK_SELECTED_TAG,
                        stackTag = BACK_STACK_FEATURED_TO_SELECTED_TAG
                    )
                    selectedBookFragment.refreshFragmentData(selectedBook!!, bookOpinions)
                }
            }
        }
    }

    fun getActionBooks() = mActionBooks
    fun getAdventureBooks() = mAdventureBooks
    fun getComedyBooks() = mComedyBooks
    fun getDramaBooks() = mDramaBooks
    fun getFantasyBooks() = mFantasyBooks
    fun getFictionBooks() = mFictionBooks
    fun getHorrorBooks() = mHorrorBooks
    fun getMythologyBooks() = mMythologyBooks
    fun getRomanceBooks() = mRomanceBooks
    fun getSatireBooks() = mSatireBooks

}