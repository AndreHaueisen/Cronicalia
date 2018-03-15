package com.andrehaueisen.cronicalia.c_creations.mvp

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.andrehaueisen.cronicalia.PARCELABLE_USER
import com.andrehaueisen.cronicalia.R
import com.andrehaueisen.cronicalia.models.User

/**
 * Created by andre on 2/19/2018.
 */
class MyCreationsPresenterFragment : Fragment() {

    companion object {

        fun newInstance(bundle: Bundle? = null): MyCreationsPresenterFragment {

            val fragment = MyCreationsPresenterFragment()
            bundle?.let {
                fragment.arguments = bundle
            }

            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val user = arguments!!.getParcelable<User>(PARCELABLE_USER)

        val rootView = inflater.inflate(R.layout.c_fragment_my_creations, container, false)

        MyCreationsView(context!!, rootView, user)

        return rootView
    }
}