package com.andrehaueisen.cronicalia.c_creations

import android.content.Intent
import android.net.Uri
import android.support.v4.util.ArraySet
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import com.andrehaueisen.cronicalia.PDF_EDIT_CODE
import com.andrehaueisen.cronicalia.R
import com.andrehaueisen.cronicalia.c_creations.mvp.MyCreationEditViewFragment
import com.andrehaueisen.cronicalia.models.Book
import com.andrehaueisen.cronicalia.utils.extensions.getFileTitle
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.item_chapter_file_edition.view.*
import studio.carbonylgroup.textfieldboxes.TextFieldBoxes

/**
 * Created by andre on 3/25/2018.
 */
class EditionFilesAdapter(
    private val mFragment: MyCreationEditViewFragment,
    private val mRecyclerView: RecyclerView,
    private val mBookIsolated: Book,
    private val mFileUrisToBeDeleted: ArraySet<String>) : RecyclerView.Adapter<EditionFilesAdapter.SelectedFileHolder>() {

    private val LOG_TAG: String = EditionFilesAdapter::class.java.simpleName
    private var mLastClickedViewHolder: SelectedFileHolder? = null
    private var mLastClickedLayoutPosition: Int = -1

    override fun getItemCount(): Int {
        return if (mBookIsolated.isLaunchedComplete) 1 else mBookIsolated.remoteChapterTitles.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectedFileHolder {

        val view = if (!mBookIsolated.isLaunchedComplete) {
            LayoutInflater.from(mFragment.context)
                .inflate(R.layout.item_chapter_file_edition, parent, false)
        } else {
            LayoutInflater.from(mFragment.context)
                .inflate(R.layout.item_book_file_edition, parent, false)
        }

        return SelectedFileHolder(view)
    }

    override fun onBindViewHolder(holder: SelectedFileHolder, position: Int) {
        if (!mBookIsolated.isLaunchedComplete) {
            holder.bindChaptersToView()
        } else {
            holder.bindBookToView()
        }
    }

    fun onEditFileReady(uri: Uri) {
        val newFileTitle = mFragment.context!!.getFileTitle(uri)

        val previousUri = mBookIsolated.remoteChapterUris[mLastClickedLayoutPosition]
        if (previousUri.startsWith("https://firebasestorage"))
            mFileUrisToBeDeleted.add(previousUri)

        if (mBookIsolated.isLaunchedComplete) {
            mBookIsolated.localFullBookUri = uri.toString()
            mLastClickedViewHolder!!.mFullBookFileNameTextView!!.text = newFileTitle

        } else {
            mBookIsolated.remoteChapterUris[mLastClickedLayoutPosition] = uri.toString()
            mBookIsolated.remoteChapterTitles[mLastClickedLayoutPosition] = newFileTitle!!

            notifyItemChanged(mLastClickedLayoutPosition)
            mLastClickedViewHolder!!.mTitleTextInput!!.extended_edition_edit_text.setText(newFileTitle)
        }

        mFragment.notifyChangeOnFileDetected()
        Log.d(LOG_TAG, mBookIsolated.toString())
    }

    fun onAddFileReady(uri: Uri){
        val newFileTitle = mFragment.context!!.getFileTitle(uri)

        mBookIsolated.remoteChapterUris.add(uri.toString())
        mBookIsolated.remoteChapterTitles.add(newFileTitle!!)

        notifyItemInserted(mBookIsolated.remoteChapterTitles.size)
        mFragment.notifyChangeOnFileDetected()
        Log.d(LOG_TAG, mBookIsolated.toString())
    }

    fun clearData() {
        mBookIsolated.remoteChapterUris.clear()
        mBookIsolated.remoteChapterTitles.clear()
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
                if (title.replace(" ", "").length > mFragment.context!!.resources.getInteger(R.integer.title_text_box_max_length) || title.isBlank()) {
                    areValid = false
                    break@loop
                }
            }
        }

        return areValid
    }

    inner class SelectedFileHolder(fileView: View) : RecyclerView.ViewHolder(fileView) {

        internal val mTitleTextInput: TextFieldBoxes? = fileView.findViewById(R.id.title_text_box)
        internal val mFullBookFileNameTextView: TextView? = fileView.findViewById(R.id.full_book_file_name_text_view)
        private val mPushFileUpButton = fileView.findViewById<ImageButton>(R.id.push_file_up_button)
        private val mChangeFileButton = fileView.findViewById<Button>(R.id.change_file_button)
        private val mRemoveFileButton = fileView.findViewById<Button>(R.id.remove_file_button)

        fun bindBookToView(){
            mFullBookFileNameTextView!!.text = mBookIsolated.title
            mChangeFileButton.setOnClickListener { editItem() }
        }

        fun bindChaptersToView() {

            if (layoutPosition == 0) {
                mPushFileUpButton.visibility = View.INVISIBLE
            } else {
                mPushFileUpButton.visibility = View.VISIBLE
            }

            if (mBookIsolated.isLaunchedComplete) {
                mRemoveFileButton.visibility = View.GONE
            }

            mChangeFileButton.setOnClickListener { editItem() }

            mRemoveFileButton.setOnClickListener {
                if( mBookIsolated.remoteChapterTitles.size > 1)
                    removeItem()
                else
                    Toasty.info(mFragment.context!!, mFragment.context!!.getString(R.string.one_file_selected_warning)).show()
            }

            mPushFileUpButton.setOnClickListener { swapItems() }

            mTitleTextInput!!.extended_edition_edit_text.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(editable: Editable) {
                    val title = editable.toString()

                    if (title.isNotBlank()) {
                        mBookIsolated.remoteChapterTitles[layoutPosition] = title
                    }

                }

                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            })

            if (mBookIsolated.isLaunchedComplete) {
                mTitleTextInput.extended_edition_edit_text!!.setText(mBookIsolated.title)
                mTitleTextInput.labelText = mFragment.getString(R.string.book_title)
            } else {
                mTitleTextInput.extended_edition_edit_text!!.setText( mBookIsolated.remoteChapterTitles[layoutPosition])
                mTitleTextInput.labelText = mFragment.getString(R.string.chapter_number_hint, position + 1)
            }
        }

        private fun editItem(){
            mLastClickedViewHolder = mRecyclerView.findViewHolderForLayoutPosition(layoutPosition) as SelectedFileHolder
            mLastClickedLayoutPosition = layoutPosition

            launchPdfSelector()
        }

        private fun launchPdfSelector() {
            val allowMultipleFiles = false

            val intent = Intent()
            intent.type = "application/pdf"
            intent.action = Intent.ACTION_GET_CONTENT
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, allowMultipleFiles)

            mFragment.startActivityForResult(Intent.createChooser(intent, "Select Pdf"), PDF_EDIT_CODE, null)
        }

        private fun removeItem(){

            val uri = mBookIsolated.remoteChapterUris[layoutPosition]
            if (uri.startsWith("https://firebasestorage"))
                mFileUrisToBeDeleted.add(uri)

            mBookIsolated.remoteChapterUris.removeAt(layoutPosition)
            mBookIsolated.remoteChapterTitles.removeAt(layoutPosition)

            if(layoutPosition + 1 < itemCount) {
                val bellowClickedViewHolder = mRecyclerView.findViewHolderForLayoutPosition(layoutPosition + 1) as SelectedFileHolder
                bellowClickedViewHolder.mTitleTextInput!!.labelText = mFragment.getString(R.string.chapter_number_hint, layoutPosition)

                if(layoutPosition == 0) {
                    bellowClickedViewHolder.mPushFileUpButton.visibility = View.INVISIBLE
                }
            }

            notifyItemRemoved(layoutPosition)
            mFragment.notifyChangeOnFileDetected()
            Log.d(LOG_TAG, mBookIsolated.toString())
        }

        /**
         * Swap uri and title layoutPosition with layoutPosition -1
         * **/
        private fun swapItems() {

            val aboveClickedViewHolder = mRecyclerView.findViewHolderForLayoutPosition(layoutPosition - 1) as SelectedFileHolder
            val clickedViewHolder = mRecyclerView.findViewHolderForLayoutPosition(layoutPosition) as SelectedFileHolder

            clickedViewHolder.mTitleTextInput!!.labelText = mFragment.getString(R.string.chapter_number_hint, layoutPosition)
            aboveClickedViewHolder.mTitleTextInput!!.labelText = mFragment.getString(R.string.chapter_number_hint, layoutPosition + 1)

            if (layoutPosition == 1) {
                clickedViewHolder.mPushFileUpButton.visibility = View.INVISIBLE
                aboveClickedViewHolder.mPushFileUpButton.visibility = View.VISIBLE
            }

            notifyItemMoved(layoutPosition, layoutPosition - 1)

            val ascendingUri = mBookIsolated.remoteChapterUris[layoutPosition]
            val ascendingTitle =  mBookIsolated.remoteChapterTitles[layoutPosition]

            mBookIsolated.remoteChapterUris[layoutPosition] = mBookIsolated.remoteChapterUris[layoutPosition - 1]
            mBookIsolated.remoteChapterUris[layoutPosition - 1] = ascendingUri

            mBookIsolated.remoteChapterTitles[layoutPosition] =  mBookIsolated.remoteChapterTitles[layoutPosition - 1]
            mBookIsolated.remoteChapterTitles[layoutPosition - 1] = ascendingTitle

            mFragment.notifyChangeOnFileDetected()

            Log.d(LOG_TAG, mBookIsolated.toString())
        }

    }

}