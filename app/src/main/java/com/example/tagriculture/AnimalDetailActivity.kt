package com.example.tagriculture

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputEditText
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.activity.viewModels
import com.example.tagriculture.viewmodels.AnimalDetailViewModel

class AnimalDetailActivity : AppCompatActivity() {

    private var nfcTagId: String? = null
    private var animalId: Long? = null

    // UI elements
    private lateinit var toolbar: Toolbar
    private lateinit var animalTypeSpinner: AutoCompleteTextView
    private val viewModel: AnimalDetailViewModel by viewModels()
    private var selectedBirthDate: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_animal_detail)

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        animalTypeSpinner = findViewById(R.id.animal_type_spinner)

        val birthDateEditText: TextInputEditText = findViewById(R.id.edit_text_birth_date)

        birthDateEditText.setOnClickListener {
            showDatePicker()
        }

        nfcTagId = intent.getStringExtra("NFC_TAG_ID")
        val receivedAnimalId = intent.getLongExtra("ANIMAL_ID", -1L)
        if (receivedAnimalId != -1L) {
            animalId = receivedAnimalId
        }

        if (animalId != null) {
            title = "Edit Animal Details"
            Log.d("AnimalDetail", "Editing mode for animal ID: $animalId")
            // TODO: Load existing animal data from the database
        } else if (nfcTagId != null) {
            title = "Register New Animal"
            Log.d("AnimalDetail", "Registration mode for tag ID: $nfcTagId")
        } else {
            title = "Error"
            Toast.makeText(this, "No animal or tag ID provided", Toast.LENGTH_LONG).show()
            finish()
        }

        setupAnimalTypeSpinner()
    }

    private fun setupAnimalTypeSpinner() {
        val animalTypes = listOf("Cattle", "Pig", "Sheep", "Goat", "Horse", "Buffalo")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, animalTypes)
        animalTypeSpinner.setAdapter(adapter)
    }

    // --- Toolbar Menu Setup ---
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_animal_detail, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_save -> {
                saveAnimal()
                true
            }
            android.R.id.home -> {
                onBackPressedDispatcher.onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun saveAnimal() {
        val nameEditText: TextInputEditText = findViewById(R.id.edit_text_name)
        val breedEditText: TextInputEditText = findViewById(R.id.edit_text_breed)
        val birthWeightEditText: TextInputEditText = findViewById(R.id.edit_text_birth_weight)

        val type = animalTypeSpinner.text.toString()
        val name = nameEditText.text.toString()
        val breed = breedEditText.text.toString()
        val birthWeight = birthWeightEditText.text.toString().toDoubleOrNull() ?: 0.0

        if (nfcTagId == null) {
            Toast.makeText(this, "Error: NFC Tag ID is missing", Toast.LENGTH_SHORT).show()
            return
        }
        if (type.isBlank() || name.isBlank()) {
            Toast.makeText(this, "Please fill in Animal Type and Name", Toast.LENGTH_SHORT).show()
            return
        }
        if (selectedBirthDate == null) {
            Toast.makeText(this, "Please select a Birth Date", Toast.LENGTH_SHORT).show()
            return
        }

        viewModel.saveNewAnimal(
            nfcTagId = nfcTagId!!,
            animalType = type,
            name = name,
            breed = breed,
            birthDate = selectedBirthDate!!,
            birthWeight = birthWeight
        )

        Toast.makeText(this, "$name has been registered!", Toast.LENGTH_LONG).show()
        finish()
    }

    private fun showDatePicker() {
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select Birth Date")
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
            .build()

        datePicker.addOnPositiveButtonClickListener { selection ->
            selectedBirthDate = selection

            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val dateString = simpleDateFormat.format(Date(selection))

            val birthDateEditText: TextInputEditText = findViewById(R.id.edit_text_birth_date)
            birthDateEditText.setText(dateString)
        }

        datePicker.show(supportFragmentManager, "DATE_PICKER_TAG")
    }
}