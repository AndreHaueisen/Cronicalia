package com.andrehaueisen.cronicalia.c_my_books

import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.text.InputType
import android.text.SpannableStringBuilder
import android.view.WindowManager
import android.widget.Button
import com.andrehaueisen.cronicalia.R
import com.andrehaueisen.cronicalia.c_my_books.mvp.MyBookEditViewFragment
import com.andrehaueisen.cronicalia.models.Book
import com.andrehaueisen.cronicalia.utils.extensions.isBookTitleValid
import com.andrehaueisen.cronicalia.utils.extensions.isSynopsisValid
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.c_dialog_edit_text.view.*
import studio.carbonylgroup.textfieldboxes.TextFieldBoxes

/**
 * Created by andre on 3/21/2018.
 */
class EditTextDialog(
    private val fragment: MyBookEditViewFragment,
    private val viewBeingEdited: ViewBeingEdited,
    private val mBook: Book
) : AlertDialog(fragment.context!!) {

    private lateinit var mGeneralTextBox: TextFieldBoxes
    private lateinit var mOkButton: Button
    private lateinit var mCancelButton: Button

    interface BookChangesListener {
        fun notifyTitleChange(title: String)
        fun notifySynopsisChange(synopsis: String)
    }

    enum class ViewBeingEdited {
        TITLE_VIEW, SYNOPSIS_VIEW
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.c_dialog_edit_text)
        setCancelable(true)

        mGeneralTextBox = findViewById(R.id.general_text_box)!!
        mOkButton = findViewById(R.id.ok_button)!!
        mCancelButton = findViewById(R.id.cancel_button)!!

        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM)

        when(viewBeingEdited){
            ViewBeingEdited.TITLE_VIEW -> {
                val title = SpannableStringBuilder(mBook.title ?: "")
                mGeneralTextBox.labelText = context.getString(R.string.book_title)
                mGeneralTextBox.maxCharacters = context.resources.getInteger(R.integer.title_text_box_max_length)
                mGeneralTextBox.general_extended_edit_text.inputType = InputType.TYPE_TEXT_FLAG_CAP_WORDS
                mGeneralTextBox.general_extended_edit_text.text = title
                mGeneralTextBox.general_extended_edit_text.setSelection(title.length)
            }

            ViewBeingEdited.SYNOPSIS_VIEW -> {
                val synopsis = SpannableStringBuilder(mBook.synopsis ?: "")
                mGeneralTextBox.labelText = context.getString(R.string.book_synopsis)
                mGeneralTextBox.maxCharacters = context.resources.getInteger(R.integer.synopsis_text_box_max_length)
                mGeneralTextBox.general_extended_edit_text.inputType = InputType.TYPE_TEXT_FLAG_MULTI_LINE or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
                mGeneralTextBox.general_extended_edit_text.text = SpannableStringBuilder(mBook.synopsis ?: "")
                mGeneralTextBox.general_extended_edit_text.setSelection(synopsis.length)
            }
        }

        mOkButton.setOnClickListener {
            when (viewBeingEdited) {
                ViewBeingEdited.TITLE_VIEW -> {
                    val newTitle = mGeneralTextBox.general_extended_edit_text.text.toString()
                    if (newTitle.isBookTitleValid(context)) {
                        mBook.title = newTitle
                        fragment.notifyTitleChange(newTitle)
                        dismiss()
                    } else {
                        Toasty.error(context, context.getString(R.string.invalid_text_detected)).show()
                    }

                }
                ViewBeingEdited.SYNOPSIS_VIEW -> {
                    val newSynopsis = mGeneralTextBox.general_extended_edit_text.text.toString()
                    if (newSynopsis.isSynopsisValid(context)) {
                        mBook.synopsis = newSynopsis
                        fragment.notifySynopsisChange(newSynopsis)
                        dismiss()
                    } else {
                        Toasty.error(context, context.getString(R.string.invalid_text_detected)).show()
                    }
                }
            }


        }

        mCancelButton.setOnClickListener { dismiss() }
    }

}