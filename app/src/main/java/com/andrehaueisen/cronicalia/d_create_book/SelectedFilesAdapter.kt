package com.andrehaueisen.cronicalia.d_create_book

import android.net.Uri
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup

/**
 * Created by andre on 2/24/2018.
 */
class SelectedFilesAdapter(private val mContext, val filesUris: ArrayList<Uri>): RecyclerView.Adapter<SelectedFilesAdapter.SelectedFileHolder>() {


    override fun getItemCount() = filesUris.size


    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): SelectedFileHolder {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onBindViewHolder(holder: SelectedFileHolder?, position: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    inner class SelectedFileHolder(fileView: View): RecyclerView.ViewHolder(fileView) {

    }

}