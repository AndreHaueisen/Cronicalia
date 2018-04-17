package com.andrehaueisen.cronicalia.f_manage_account

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.text.InputType
import android.text.SpannableStringBuilder
import android.view.WindowManager
import android.widget.Button
import com.andrehaueisen.cronicalia.R
import com.andrehaueisen.cronicalia.f_manage_account.mvp.ManageAccountView
import com.andrehaueisen.cronicalia.models.User
import com.andrehaueisen.cronicalia.utils.extensions.isAboutMeTextValid
import com.andrehaueisen.cronicalia.utils.extensions.isArtisticNameValid
import com.andrehaueisen.cronicalia.utils.extensions.isUserNameValid
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.c_dialog_edit_text.view.*
import studio.carbonylgroup.textfieldboxes.TextFieldBoxes

/**
 * Created by andre on 3/21/2018.
 */
class EditTextDialog(
    context: Context,
    private val view: ManageAccountView,
    private val viewBeingEdited: ViewBeingEdited,
    private val mUserIsolated: User
) : AlertDialog(context) {

    private lateinit var mGeneralTextBox: TextFieldBoxes
    private lateinit var mOkButton: Button
    private lateinit var mCancelButton: Button

    interface UserChangesListener {
        fun notifyNameChange(name: String)
        fun notifyArtisticNameChange(artisticName: String)
        fun notifyAboutMeChange(aboutMe: String)
    }

    enum class ViewBeingEdited {
        NAME_VIEW, ARTISTIC_NAME, ABOUT_ME_VIEW
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
            ViewBeingEdited.NAME_VIEW -> {
                val userName = SpannableStringBuilder(mUserIsolated.name ?: "")
                mGeneralTextBox.labelText = context.getString(R.string.user_name)
                mGeneralTextBox.maxCharacters = context.resources.getInteger(R.integer.title_text_box_max_length)
                mGeneralTextBox.general_extended_edit_text.inputType = InputType.TYPE_TEXT_FLAG_CAP_WORDS
                mGeneralTextBox.general_extended_edit_text.text = userName
                mGeneralTextBox.general_extended_edit_text.setSelection(userName.length)
            }

            ViewBeingEdited.ARTISTIC_NAME -> {
                val artisticName = SpannableStringBuilder(mUserIsolated.artisticName ?: "")
                mGeneralTextBox.labelText = context.getString(R.string.artistic_name)
                mGeneralTextBox.maxCharacters = context.resources.getInteger(R.integer.title_text_box_max_length)
                mGeneralTextBox.general_extended_edit_text.inputType = InputType.TYPE_TEXT_FLAG_CAP_WORDS
                mGeneralTextBox.general_extended_edit_text.text = artisticName
                mGeneralTextBox.general_extended_edit_text.setSelection(artisticName.length)
            }

            ViewBeingEdited.ABOUT_ME_VIEW -> {
                val aboutMe = SpannableStringBuilder(mUserIsolated.aboutMe ?: "")
                mGeneralTextBox.labelText = context.getString(R.string.about_me)
                mGeneralTextBox.maxCharacters = context.resources.getInteger(R.integer.synopsis_text_box_max_length)
                mGeneralTextBox.general_extended_edit_text.inputType = InputType.TYPE_TEXT_FLAG_MULTI_LINE or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
                mGeneralTextBox.general_extended_edit_text.text = aboutMe
                mGeneralTextBox.general_extended_edit_text.setSelection(aboutMe.length)
            }
        }

        mOkButton.setOnClickListener {
            when (viewBeingEdited) {
                ViewBeingEdited.NAME_VIEW -> {
                    val newName = mGeneralTextBox.general_extended_edit_text.text.toString()
                    if (newName.isUserNameValid(context)) {
                        mUserIsolated.name = newName
                        view.notifyNameChange(newName)
                        dismiss()
                    } else {
                        Toasty.error(context, context.getString(R.string.invalid_text_detected)).show()
                    }

                }

                ViewBeingEdited.ARTISTIC_NAME -> {
                    val newArtisticName = mGeneralTextBox.general_extended_edit_text.text.toString()
                    if(newArtisticName.isArtisticNameValid(context)){
                        mUserIsolated.artisticName = newArtisticName
                        view.notifyArtisticNameChange(newArtisticName)
                        dismiss()
                    } else {
                        Toasty.error(context, context.getString(R.string.invalid_text_detected)).show()
                    }
                }

                ViewBeingEdited.ABOUT_ME_VIEW -> {
                    val newAboutMe = mGeneralTextBox.general_extended_edit_text.text.toString()
                    if (newAboutMe.isAboutMeTextValid(context)) {
                        mUserIsolated.aboutMe = newAboutMe
                        view.notifyAboutMeChange(newAboutMe)
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