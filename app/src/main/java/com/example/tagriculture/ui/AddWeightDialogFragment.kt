package com.example.tagriculture.ui

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.tagriculture.R
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputEditText
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AddWeightDialogFragment : DialogFragment() {

    interface AddWeightDialogListener {
        fun onWeightEntryConfirmed(weight: Double, date: Long)
    }

    private lateinit var weightEditText: TextInputEditText
    private lateinit var dateEditText: TextInputEditText
    private var selectedDate: Long = System.currentTimeMillis()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_add_weight, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        weightEditText = view.findViewById(R.id.edit_text_dialog_weight)
        dateEditText = view.findViewById(R.id.edit_text_dialog_date)
        val saveButton: Button = view.findViewById(R.id.btn_save_weight)
        val cancelButton: Button = view.findViewById(R.id.btn_cancel)

        updateDateText()
        val currentWeight = arguments?.getDouble(ARG_CURRENT_WEIGHT) ?: 0.0
        weightEditText.setText(currentWeight.toString())

        dateEditText.setOnClickListener { showDatePicker() }
        cancelButton.setOnClickListener { dismiss() }

        saveButton.setOnClickListener {
            val weightString = weightEditText.text.toString()
            if (weightString.isBlank()) {
                Toast.makeText(context, "Please enter a weight", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val weight = weightString.toDouble()
            (activity as? AddWeightDialogListener)?.onWeightEntryConfirmed(weight, selectedDate)
            dismiss()
        }
    }

    private fun showDatePicker() {
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select Measurement Date")
            .setSelection(selectedDate)
            .build()
        datePicker.addOnPositiveButtonClickListener { selection ->
            selectedDate = selection
            updateDateText()
        }
        datePicker.show(childFragmentManager, "WEIGHT_DATE_PICKER")
    }

    private fun updateDateText() {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        dateEditText.setText(sdf.format(Date(selectedDate)))
    }

    companion object {
        private const val ARG_CURRENT_WEIGHT = "current_weight"

        fun newInstance(currentWeight: Double): AddWeightDialogFragment {
            val args = Bundle().apply {
                putDouble(ARG_CURRENT_WEIGHT, currentWeight)
            }
            val fragment = AddWeightDialogFragment()
            fragment.arguments = args
            return fragment
        }
    }
}