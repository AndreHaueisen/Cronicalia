package com.andrehaueisen.cronicalia.g_manage_account

import android.app.Activity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.text.Editable
import android.text.InputType
import android.text.SpannableStringBuilder
import android.text.TextWatcher
import android.text.method.ScrollingMovementMethod
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import com.andrehaueisen.cronicalia.R
import com.andrehaueisen.cronicalia.g_manage_account.mvp.ManageAccountView
import com.andrehaueisen.cronicalia.models.User
import com.andrehaueisen.cronicalia.utils.extensions.isAboutMeTextValid
import com.andrehaueisen.cronicalia.utils.extensions.isTwitterProfileValid
import com.andrehaueisen.cronicalia.utils.extensions.isUserNameValid
import com.andrehaueisen.cronicalia.utils.extensions.showErrorMessage
import kotlinx.android.synthetic.main.dialog_edit_text.view.*
import studio.carbonylgroup.textfieldboxes.TextFieldBoxes

/**
 * Created by andre on 3/21/2018.
 */
class EditTextDialog(
    private val activity: Activity,
    private val view: ManageAccountView,
    private val viewBeingEdited: ViewBeingEdited,
    private val mUserIsolated: User
) : AlertDialog(activity) {

    private lateinit var mFullTextView: TextView
    private lateinit var mGeneralTextBox: TextFieldBoxes
    private lateinit var mOkButton: Button
    private lateinit var mCancelButton: Button

    interface UserChangesListener {
        fun notifyNameChange(name: String)
        fun notifyTwitterProfileChange(twitterProfile: String)
        fun notifyAboutMeChange(aboutMe: String)
    }

    enum class ViewBeingEdited {
        NAME_VIEW, TWITTER_LOCATOR, ABOUT_ME_VIEW
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.dialog_edit_text)
        setCancelable(true)

        mFullTextView = findViewById(R.id.full_text_view)!!
        mFullTextView.movementMethod = ScrollingMovementMethod.getInstance()
        mGeneralTextBox = findViewById(R.id.general_text_box)!!
        mOkButton = findViewById(R.id.ok_button)!!
        mCancelButton = findViewById(R.id.cancel_button)!!

        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM)

        when(viewBeingEdited){
            ViewBeingEdited.NAME_VIEW -> {
                val userName = SpannableStringBuilder(mUserIsolated.name ?: "")
                mFullTextView.text = userName
                mGeneralTextBox.labelText = context.getString(R.string.user_name)
                mGeneralTextBox.maxCharacters = context.resources.getInteger(R.integer.title_text_box_max_length)
                mGeneralTextBox.general_extended_edit_text.inputType = InputType.TYPE_TEXT_FLAG_CAP_WORDS
                mGeneralTextBox.general_extended_edit_text.text = userName
                mGeneralTextBox.general_extended_edit_text.setSelection(userName.length)
            }

            ViewBeingEdited.TWITTER_LOCATOR -> {
                val twitterProfile = SpannableStringBuilder(if(mUserIsolated.twitterProfile != null) mUserIsolated.twitterProfile else "")
                mFullTextView.text = twitterProfile
                mGeneralTextBox.labelText = context.getString(R.string.twitter_profile)
                mGeneralTextBox.maxCharacters = context.resources.getInteger(R.integer.title_text_box_max_length)
                mGeneralTextBox.general_extended_edit_text.inputType = InputType.TYPE_TEXT_FLAG_CAP_WORDS
                mGeneralTextBox.general_extended_edit_text.text = twitterProfile
                mGeneralTextBox.general_extended_edit_text.setSelection(twitterProfile.length)
            }

            ViewBeingEdited.ABOUT_ME_VIEW -> {
                mFullTextView.visibility = View.VISIBLE
                val aboutMe = SpannableStringBuilder(mUserIsolated.aboutMe ?: "")
                mFullTextView.text = aboutMe
                mGeneralTextBox.labelText = context.getString(R.string.about_me)
                mGeneralTextBox.maxCharacters = context.resources.getInteger(R.integer.synopsis_text_box_max_length)
                mGeneralTextBox.general_extended_edit_text.inputType = InputType.TYPE_TEXT_FLAG_MULTI_LINE or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
                mGeneralTextBox.general_extended_edit_text.text = aboutMe
                mGeneralTextBox.general_extended_edit_text.setSelection(aboutMe.length)
            }
        }

        mGeneralTextBox.general_extended_edit_text.addTextChangedListener(object : TextWatcher{
            override fun afterTextChanged(text: Editable?) {
                mFullTextView.text = text!!.toString()
            }

            override fun onTextChanged(text: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        })

        mOkButton.setOnClickListener {
            when (viewBeingEdited) {
                ViewBeingEdited.NAME_VIEW -> {
                    val newName = mGeneralTextBox.general_extended_edit_text.text.toString()
                    if (newName.isUserNameValid(context)) {
                        mUserIsolated.name = newName
                        view.notifyNameChange(mUserIsolated.name!!)
                        dismiss()
                    } else {
                        activity.showErrorMessage(R.string.invalid_text_detected)
                    }

                }

                ViewBeingEdited.TWITTER_LOCATOR -> {
                    val twitterLocator = mGeneralTextBox.general_extended_edit_text.text.toString()
                    if(twitterLocator.isTwitterProfileValid(context)){
                        mUserIsolated.twitterProfile = if(twitterLocator.first() != '@') "@$twitterLocator" else twitterLocator
                        view.notifyTwitterProfileChange(mUserIsolated.twitterProfile!!)
                        dismiss()
                    } else {
                        activity.showErrorMessage(R.string.invalid_text_detected)
                    }
                }

                ViewBeingEdited.ABOUT_ME_VIEW -> {
                    val newAboutMe = mGeneralTextBox.general_extended_edit_text.text.toString()
                    if (newAboutMe.isAboutMeTextValid(context)) {
                        mUserIsolated.aboutMe = newAboutMe
                        view.notifyAboutMeChange(mUserIsolated.aboutMe!!)
                        dismiss()
                    } else {
                        activity.showErrorMessage(R.string.invalid_text_detected)
                    }
                }
            }
        }

        mCancelButton.setOnClickListener { dismiss() }
    }

}