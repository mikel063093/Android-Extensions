package com.tunjid.androidbootstrap.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.tunjid.androidbootstrap.R
import com.tunjid.androidbootstrap.adapters.DoggoAdapter.ImageListAdapterListener
import com.tunjid.androidbootstrap.baseclasses.AppBaseFragment
import com.tunjid.androidbootstrap.model.Doggo
import com.tunjid.androidbootstrap.viewholders.DoggoViewHolder

class DoggoFragment : AppBaseFragment(), ImageListAdapterListener {

    override fun getStableTag(): String =
            super.getStableTag() + "-" + arguments!!.getParcelable(ARG_DOGGO)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_image_detail, container, false)
        val doggo = arguments!!.getParcelable<Doggo>(ARG_DOGGO)!!

        rootView.tag = doggo
        DoggoViewHolder(rootView, this).bind(doggo)

        return rootView
    }

    override fun onDoggoImageLoaded(doggo: Doggo) = parentFragment!!.startPostponedEnterTransition()

    companion object {
        private const val ARG_DOGGO = "doggo"

        fun newInstance(doggo: Doggo): DoggoFragment = DoggoFragment().apply {
            arguments = Bundle().apply { putParcelable(ARG_DOGGO, doggo) }
        }

    }
}
