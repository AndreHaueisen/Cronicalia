package com.andrehaueisen.cronicalia.f_create_book

import android.content.Context
import android.net.Uri
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import com.andrehaueisen.cronicalia.R
import com.andrehaueisen.cronicalia.models.Book
import com.andrehaueisen.cronicalia.utils.extensions.getFileTitle
import kotlinx.android.synthetic.main.item_chapter_file.view.*
import studio.carbonylgroup.textfieldboxes.TextFieldBoxes

/**
 * Created by andre on 2/24/2018.
 */
class SelectedFilesAdapter(
    private val mContext: Context,
    private val mRecyclerView: RecyclerView,
    private val mBookIsolated: Book
) : RecyclerView.Adapter<SelectedFilesAdapter.SelectedFileHolder>() {


    override fun getItemCount(): Int {
        return if(mBookIsolated.isLaunchedComplete) 1 else mBookIsolated.remoteChapterTitles.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectedFileHolder {

        val view = if (!mBookIsolated.isLaunchedComplete) {
            LayoutInflater.from(mContext)
                .inflate(R.layout.item_chapter_file, parent, false)
        } else {
            LayoutInflater.from(mContext)
                .inflate(R.layout.item_my_book_file, parent, false)
        }

        return SelectedFileHolder(view)
    }

    override fun onBindViewHolder(holder: SelectedFileHolder, position: Int) {
        if (!mBookIsolated.isLaunchedComplete) {
            holder.bindChaptersToView(position)
        } else {
            holder.bindBookToView()
        }
    }

    fun clearData() {
        mBookIsolated.remoteChapterTitles.clear()
        mBookIsolated.remoteChapterUris.clear()

        notifyDataSetChanged()
    }

    fun areChapterTitlesValid(): Boolean {

        var areValid = true

        if (mBookIsolated.isLaunchedComplete){
            if(mBookIsolated.originalImmutableTitle != null) return areValid
        } else {

            if(mBookIsolated.remoteChapterTitles.size == 0) return false

            loop@ for(i in 0 until mBookIsolated.remoteChapterTitles.size){
                val title = mBookIsolated.remoteChapterTitles[i]
                if (title.replace(" ", "").length > mContext.resources.getInteger(R.integer.title_text_box_max_length) || title.isBlank()) {
                    areValid = false
                    break@loop
                }
            }
        }

        return areValid
    }

    inner class SelectedFileHolder(fileView: View) : RecyclerView.ViewHolder(fileView) {

        private val mPushFileUpButton = fileView.findViewById<ImageButton>(R.id.push_file_up_button)
        private val mChapterTitleTextInput = fileView.findViewById<TextFieldBoxes>(R.id.chapter_title_text_box)
        private val mFullBookFileNameTextView = fileView.findViewById<TextView>(R.id.full_book_file_name_text_view)

        fun bindBookToView() {
            mFullBookFileNameTextView.text = mContext.getFileTitle(Uri.parse(mBookIsolated.localFullBookUri))
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
                        mBookIsolated.remoteChapterTitles[position] = title
                    }
                }

                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            })

            mChapterTitleTextInput.extended_edit_text!!.setText(mBookIsolated.remoteChapterTitles[position])
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

            val ascendingUri = mBookIsolated.remoteChapterUris[layoutPosition]
            val ascendingTitle = mBookIsolated.remoteChapterTitles[layoutPosition]

            mBookIsolated.remoteChapterUris[layoutPosition] = mBookIsolated.remoteChapterUris[layoutPosition - 1]
            mBookIsolated.remoteChapterUris[layoutPosition - 1] = ascendingUri

            mBookIsolated.remoteChapterTitles[layoutPosition] = mBookIsolated.remoteChapterTitles[layoutPosition - 1]
            mBookIsolated.remoteChapterTitles[layoutPosition - 1] = ascendingTitle

            Log.d("SelectedFilesAdapter", mBookIsolated.remoteChapterUris.toArray().contentToString())
            Log.d("SelectedFilesAdapter", mBookIsolated.remoteChapterTitles.toArray().contentToString())
        }

    }


}