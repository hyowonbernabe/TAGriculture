package com.example.tagriculture.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.DialogFragment
import com.example.tagriculture.R

class NewTagDialogFragment : DialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_new_tag, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val registerButton: Button = view.findViewById(R.id.btn_register_new)
        val reassignButton: Button = view.findViewById(R.id.btn_reassign)

        registerButton.setOnClickListener {
            // TODO: Handle Register click
            dismiss()
        }

        reassignButton.setOnClickListener {
            // TODO: Handle Reassign click
            dismiss()
        }
    }
}