package com.example.tagriculture.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.DialogFragment
import com.example.tagriculture.R

class NewTagDialogFragment : DialogFragment() {

    interface NewTagDialogListener {
        fun onRegisterClicked(tagId: String)
    }

    private var serialNumber: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            serialNumber = it.getString(ARG_SERIAL_NUMBER)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_new_tag, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val registerButton: Button = view.findViewById(R.id.btn_register_new)
        val reassignButton: Button = view.findViewById(R.id.btn_reassign)

        registerButton.setOnClickListener {
            serialNumber?.let {
                (targetFragment as? NewTagDialogListener)?.onRegisterClicked(it)
            }
            dismiss()
        }
        reassignButton.setOnClickListener { /* TODO */ dismiss() }
    }

    companion object {
        private const val ARG_SERIAL_NUMBER = "serial_number"

        fun newInstance(serialNumber: String): NewTagDialogFragment {
            val args = Bundle().apply {
                putString(ARG_SERIAL_NUMBER, serialNumber)
            }
            val fragment = NewTagDialogFragment()
            fragment.arguments = args
            return fragment
        }
    }
}