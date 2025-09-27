package com.example.tagriculture

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView

class ProfileFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_placeholder, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val titleView: TextView = view.findViewById(R.id.placeholder_title)
        val textView: TextView = view.findViewById(R.id.placeholder_text)
        val button: Button = view.findViewById(R.id.btn_tutorial)

        titleView.text = "Profile"
        textView.text = "This screen is a placeholder for the User Profile. This area is intended for managing a user's account and farm information. Users will be able to edit their public details, adjust app settings, and view their personal activity within the TAGriculture app."

        button.setOnClickListener {
            val intent = Intent(requireActivity(), OnboardingActivity::class.java)
            startActivity(intent)
        }
    }
}