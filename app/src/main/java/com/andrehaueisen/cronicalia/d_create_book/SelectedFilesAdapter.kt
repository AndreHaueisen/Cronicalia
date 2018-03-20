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
import kotlinx.android.synthetic.main.item_chapter.view.*
import studio.carbonylgroup.textfieldboxes.TextFieldBoxes

/**
 * Created by andre on 2/24/2018.
 */
class SelectedFilesAdapter(
    private val mContext: Context,
    private val mRecyclerView: RecyclerView,
    private val mMapChapterUriTitle: LinkedHashMap<String, String> = linkedMapOf(),
    private var mBookFileTitle: String? = null
) : RecyclerView.Adapter<SelectedFilesAdapter.SelectedFileHolder>() {

    private val mUris: ArrayList<String>?
    private val mTitles: ArrayList<String>?

    init {
        if (mBookFileTitle == null) {
            mUris = ArrayList(mMapChapterUriTitle.keys)
            mTitles = ArrayList(mMapChapterUriTitle.values)
        } else {
            mUris = null
            mTitles = null
        }
    }

    override fun getItemCount(): Int {
        return if(mBookFileTitle == null) mMapChapterUriTitle.size else 1
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectedFileHolder {

        val view = if (mBookFileTitle == null) {
            LayoutInflater.from(mContext)
                .inflate(R.layout.item_chapter, parent, false)
        } else {
            LayoutInflater.from(mContext)
                .inflate(R.layout.item_book_representation, parent, false)
        }

        return SelectedFileHolder(view)
    }

    override fun onBindViewHolder(holder: SelectedFileHolder, position: Int) {
        if (mBookFileTitle == null) {
            holder.bindChaptersToView(position)
        } else {
            holder.bindBookToView()
        }
    }

    fun clearData() {
        mMapChapterUriTitle.clear()
        mUris?.clear()
        mTitles?.clear()
        mBookFileTitle = null
        notifyDataSetChanged()
    }

    fun organizeLinkedMap(){
        if (mBookFileTitle == null) {
            mMapChapterUriTitle.clear()
            mUris!!.forEachIndexed { index, uri -> mMapChapterUriTitle[uri] = mTitles!![index] }
        }
    }

    fun areChapterTitlesValid(): Boolean {

        var areValid = true

        if(mBookFileTitle != null) return areValid

        if(mTitles == null) return false

        loop@ for(i in 0 until mTitles.size){
            val title = mTitles[i]
            if (title.replace(" ", "").length > mContext.resources.getInteger(R.integer.title_text_box_max_length) || title.isBlank()) {
                areValid = false
                break@loop
            }
        }

        return areValid
    }

    inner class SelectedFileHolder(fileView: View) : RecyclerView.ViewHolder(fileView) {

        private val mPushFileUpButton = fileView.findViewById<ImageButton>(R.id.push_file_up_button)
        private val mChapterTitleTextInput =
            fileView.findViewById<TextFieldBoxes>(R.id.chapter_title_text_box)
        private val mFullBookFileNameTextView =
            fileView.findViewById<TextView>(R.id.full_book_file_name_text_view)

        fun bindBookToView() {
            mFullBookFileNameTextView.text = mBookFileTitle
        }

        fun bindChaptersToView(position: Int) {

            if (position == 0) {
                mPushFileUpButton.visibility = View.INVISIBLE
            } else {
                mPushFileUpButton.visibility = View.VISIBLE
            }

            mPushFileUpButton.setOnClickListener {
                swapItems()
            }

            mChapterTitleTextInput.extended_edit_text.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(editable: Editable) {
                    val title = editable.toString()
                    if (title.isNotBlank()) {
                        mTitles!![position] = title
                    }
                }

                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            })

            mChapterTitleTextInput.extended_edit_text!!.setText(mTitles!![position])
            mChapterTitleTextInput.labelText =
                    mContext.getString(R.string.chapter_number_hint, position + 1)
        }

        /**
         * Swap uri and title layoutPosition with layoutPosition -1
         * **/
        private fun swapItems() {

            val clickedViewHolder =
                mRecyclerView.findViewHolderForLayoutPosition(layoutPosition) as SelectedFileHolder
            val aboveClickedViewHolder =
                mRecyclerView.findViewHolderForLayoutPosition(layoutPosition - 1) as SelectedFileHolder

            clickedViewHolder.mChapterTitleTextInput.labelText =
                    mContext.getString(R.string.chapter_number_hint, layoutPosition)
            aboveClickedViewHolder.mChapterTitleTextInput.labelText =
                    mContext.getString(R.string.chapter_number_hint, layoutPosition + 1)

            if (layoutPosition == 1) {
                clickedViewHolder.mPushFileUpButton.visibility = View.INVISIBLE
                aboveClickedViewHolder.mPushFileUpButton.visibility = View.VISIBLE
            }

            notifyItemMoved(layoutPosition, layoutPosition - 1)

            val ascendingUri = mUris!![layoutPosition]
            val ascendingTitle = mTitles!![layoutPosition]

            mUris[layoutPosition] = mUris[layoutPosition - 1]
            mUris[layoutPosition - 1] = ascendingUri

            mTitles[layoutPosition] = mTitles[layoutPosition - 1]
            mTitles[layoutPosition - 1] = ascendingTitle

        }

    }


}