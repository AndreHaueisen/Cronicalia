package com.andrehaueisen.cronicalia.d_create_book

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import com.andrehaueisen.cronicalia.R
import kotlinx.android.synthetic.main.item_chapter_representation.view.*
import studio.carbonylgroup.textfieldboxes.TextFieldBoxes

/**
 * Created by andre on 2/24/2018.
 */
class SelectedFilesAdapter(
    private val mContext: Context,
    private val mMapChapterUriTitle: HashMap<String, String>? = null,
    private var mBookFileTitle: String? = null) : RecyclerView.Adapter<SelectedFilesAdapter.SelectedFileHolder>() {

    private val mUris: ArrayList<String>?
    private val mTitles: ArrayList<String>?

    init {
        if (mMapChapterUriTitle != null) {
            mUris = ArrayList(mMapChapterUriTitle.keys)
            mTitles = ArrayList(mMapChapterUriTitle.values)
        } else {
            mUris = null
            mTitles = null
        }
    }

    override fun getItemCount(): Int {
        return mMapChapterUriTitle?.size ?: 1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectedFileHolder {

        val view = if (mMapChapterUriTitle != null) {
            LayoutInflater.from(mContext)
                .inflate(R.layout.item_chapter_representation, parent, false)
        } else {
            LayoutInflater.from(mContext)
                .inflate(R.layout.item_book_representation, parent, false)
        }

        return SelectedFileHolder(view)
    }

    override fun onBindViewHolder(holder: SelectedFileHolder, position: Int) {
        if(mMapChapterUriTitle != null) {
            holder.bindChaptersToView(position)
        } else {
            holder.bindBookToView()
        }
    }

    fun clearData(){
        mMapChapterUriTitle?.clear()
        mUris?.clear()
        mTitles?.clear()
        mBookFileTitle = null
        notifyDataSetChanged()
    }

    fun onReagementReady() {
        if(mMapChapterUriTitle != null) {
            mMapChapterUriTitle.clear()
            mUris!!.forEachIndexed { index, uri -> mMapChapterUriTitle[uri] = mTitles!![index] }
        }
    }

    inner class SelectedFileHolder(fileView: View) : RecyclerView.ViewHolder(fileView) {

        private val mPushFileUpButton = fileView.findViewById<ImageButton>(R.id.push_file_up_button)
        private val mChapterTitleTextInput = fileView.findViewById<TextFieldBoxes>(R.id.chapter_title_text_box)
        private val mFullBookFileNameTextView = fileView.findViewById<TextView>(R.id.full_book_file_name_text_view)

        fun bindBookToView(){
            mFullBookFileNameTextView.text = mBookFileTitle
        }

        fun bindChaptersToView(position: Int) {

            if (position == 0) {
                mPushFileUpButton.visibility = View.INVISIBLE
            } else {
                mPushFileUpButton.visibility = View.VISIBLE
            }

            mPushFileUpButton.setOnClickListener {
                if (position != 0) {
                    swapItems(position)
                }
            }

            mChapterTitleTextInput.extended_edit_text.addTextChangedListener(object: TextWatcher{
                override fun afterTextChanged(title: Editable?) {
                    verifyTitleValidity(title!!.toString(), position)
                }

                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            })

            mChapterTitleTextInput.extended_edit_text!!.setText(mTitles!![position])
            mChapterTitleTextInput.labelText = mContext.getString(R.string.chapter_number_hint, position)
        }

        /**
         * Swap uri and title position with position -1
         * **/
        private fun swapItems(position: Int) {

            val ascendingUri = mUris!![position]
            val ascendingTitle = mTitles!![position]

            mUris[position] = mUris[position - 1]
            mUris[position - 1] = ascendingUri

            mTitles[position] = mTitles[position - 1]
            mTitles[position - 1] = ascendingTitle

            notifyItemChanged(position)
            notifyItemChanged(position - 1)
            notifyItemMoved(position, position - 1)
        }

        private fun verifyTitleValidity(title: String, position: Int) {

            if (title.isNotBlank()) {
                mTitles!![position] = title
            }
        }
    }

}