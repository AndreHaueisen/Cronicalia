package com.andrehaueisen.cronicalia.c_creations.mvp

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.andrehaueisen.cronicalia.R
import com.andrehaueisen.cronicalia.models.User

/**
 * Created by andre on 2/19/2018.
 */
class MyCreationsPresenterFragment : Fragment() {

    private lateinit var mView : MyCreationsView
    private var mUser: User? = null

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

        mUser = (activity as MyCreationsActivity?)?.mUser

        val rootView = inflater.inflate(R.layout.c_fragment_my_creations, container, false)

        mView = MyCreationsView(context!!, rootView, mUser!!)

        return rootView
    }

    override fun onResume() {
        super.onResume()

        mView.onResume()
    }
}