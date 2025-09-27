package com.example.tagriculture

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView

class OnboardingSlideFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_onboarding_slide, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val titleView: TextView = view.findViewById(R.id.slide_title)
        val textView: TextView = view.findViewById(R.id.slide_text)
        val imageView: ImageView = view.findViewById(R.id.slide_image)

        arguments?.let {
            titleView.text = it.getString(ARG_TITLE)
            textView.text = it.getString(ARG_TEXT)
            imageView.setImageResource(it.getInt(ARG_IMAGE_RES))
        }
    }

    companion object {
        private const val ARG_TITLE = "arg_title"
        private const val ARG_TEXT = "arg_text"
        private const val ARG_IMAGE_RES = "arg_image_res"

        fun newInstance(title: String, text: String, imageRes: Int): OnboardingSlideFragment {
            val fragment = OnboardingSlideFragment()
            val args = Bundle().apply {
                putString(ARG_TITLE, title)
                putString(ARG_TEXT, text)
                putInt(ARG_IMAGE_RES, imageRes)
            }
            fragment.arguments = args
            return fragment
        }
    }
}