package com.example.tagriculture

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView

class HomeFragment : Fragment() {
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

        titleView.text = "Home"
        textView.text = "This screen is a placeholder for the future Marketplace. In a full release, this will be the central hub for users to browse, search, and filter livestock listings from other farms. It is designed to be the main e-commerce section of the TAGriculture platform."

        button.setOnClickListener {
            val intent = Intent(requireActivity(), OnboardingActivity::class.java)
            startActivity(intent)
        }
    }
}