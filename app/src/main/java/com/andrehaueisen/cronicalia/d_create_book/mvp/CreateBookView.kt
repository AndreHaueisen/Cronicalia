package com.andrehaueisen.cronicalia.d_create_book.mvp

import android.content.Context
import android.support.design.widget.TextInputLayout
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import com.andrehaueisen.cronicalia.R
import com.andrehaueisen.cronicalia.models.Book

/**
 * Created by andre on 2/22/2018.
 */
class CreateBookView(val mContext: Context, val mRootView: View) {

    private val mBook = Book(title = "")

    private val mTitleTextInput = mRootView.findViewById<TextInputLayout>(R.id.title_input_layout)
    private val mPeriodicitySpinner = mRootView.findViewById<Spinner>(R.id.periodicity_spinner)

    init {
        initiateBookImages()
        initiateTitleTextInput()
        initiateBookStatusRadioGroup()
        initiateSpinner()
        changeLaunchDescriptionText()
    }

    private fun initiateBookImages(){
        val coverImageView = mRootView.findViewById<ImageView>(R.id.book_cover_image_view)
        val posterImageView = mRootView.findViewById<ImageView>(R.id.book_poster_image_view)

        coverImageView.setOnClickListener {  }

        posterImageView.setOnClickListener {  }
    }

    private fun initiateTitleTextInput(){
        mTitleTextInput.editText!!.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(title: Editable?) {
                mBook.title = title.toString()
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        })

    }

    private fun initiateBookStatusRadioGroup(){
        val bookLaunchRadioGroup = mRootView.findViewById<RadioGroup>(R.id.book_launch_status_radio_group)
        bookLaunchRadioGroup.check(R.id.launch_full_book_radio_button)
        mBook.isComplete = true
        bookLaunchRadioGroup.setOnCheckedChangeListener(object: RadioGroup.OnCheckedChangeListener{
            override fun onCheckedChanged(radioGroup: RadioGroup, checkedId: Int) {
                when(checkedId){
                    R.id.launch_full_book_radio_button -> {
                        mBook.isComplete = true
                        mPeriodicitySpinner.visibility = View.GONE
                    }
                    R.id.launch_chapters_periodically_radio_button -> {
                        mBook.isComplete = false
                        mPeriodicitySpinner.visibility = View.VISIBLE
                    }
                }
                changeLaunchDescriptionText()
            }
        })
    }

    private fun initiateSpinner(){

        val adapter = ArrayAdapter.createFromResource(mContext, R.array.periodicity_array, R.layout.item_spinner)
        adapter.setDropDownViewResource(R.layout.item_dropdown_spinner)
        mPeriodicitySpinner.adapter = adapter

        mPeriodicitySpinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(adapter: AdapterView<*>?) {
                mBook.periodicity = Book.ChapterPeriodicity.NONE
            }

            override fun onItemSelected(adapter: AdapterView<*>?, clickedView: View?, itemPosition: Int, id: Long) {
                when(itemPosition){
                    0 -> mBook.periodicity = Book.ChapterPeriodicity.EVERY_DAY
                    1 -> mBook.periodicity = Book.ChapterPeriodicity.EVERY_3_DAYS
                    2 -> mBook.periodicity = Book.ChapterPeriodicity.EVERY_7_DAYS
                    3 -> mBook.periodicity = Book.ChapterPeriodicity.EVERY_14_DAYS
                    4 -> mBook.periodicity = Book.ChapterPeriodicity.EVERY_30_DAYS
                    5 -> mBook.periodicity = Book.ChapterPeriodicity.EVERY_42_DAYS
                }
                changeLaunchDescriptionText()
            }
        }
    }

    private fun changeLaunchDescriptionText(){
        val launchDescriptionTextView = mRootView.findViewById<TextView>(R.id.launch_description_text_view)
        val launchDescriptionArray = mRootView.resources.getStringArray(R.array.launch_description_array)

        if(mBook.isComplete){
            launchDescriptionTextView.text = launchDescriptionArray[0]
        } else {
            when (mBook.periodicity) {
                Book.ChapterPeriodicity.EVERY_DAY -> launchDescriptionTextView.text = launchDescriptionArray[1]
                else -> {
                    launchDescriptionTextView.text = String.format(launchDescriptionArray[2], mBook.periodicity.getPeriodicity())
                }
            }
        }
    }
}