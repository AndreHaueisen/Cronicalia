package com.andrehaueisen.cronicalia.f_my_books

import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import com.andrehaueisen.cronicalia.R
import com.andrehaueisen.cronicalia.f_my_books.mvp.MyBookEditViewFragment

class DeleteBookAlertDialog : DialogFragment() {

    interface DeleteBookListener {
        fun onDeletionConfirmed()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val dialogBuilder = AlertDialog.Builder(requireContext(), R.style.DeleteBookDialog)
        dialogBuilder.setTitle(R.string.delete_book_dialog_title)
        dialogBuilder.setMessage(R.string.delete_book_dialog_message)

        dialogBuilder.setPositiveButton(R.string.delete, { _, _ ->
            (targetFragment as? MyBookEditViewFragment)?.onDeletionConfirmed()
            dismiss()
        })

        dialogBuilder.setNegativeButton(R.string.cancel, { _, _ -> dismiss() })

        //val inflater = LayoutInflater.from(requireContext()).inflate()

        return dialogBuilder.create()
    }

}