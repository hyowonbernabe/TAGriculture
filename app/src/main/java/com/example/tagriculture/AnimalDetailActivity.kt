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
import com.google.android.material.textfield.TextInputEditText

class AnimalDetailActivity : AppCompatActivity() {

    private var nfcTagId: String? = null
    private var animalId: Long? = null

    // UI elements
    private lateinit var toolbar: Toolbar
    private lateinit var animalTypeSpinner: AutoCompleteTextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_animal_detail)

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        animalTypeSpinner = findViewById(R.id.animal_type_spinner)

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
                // TODO: Handle save logic
                Toast.makeText(this, "Save clicked!", Toast.LENGTH_SHORT).show()
                true
            }
            android.R.id.home -> {
                onBackPressedDispatcher.onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}