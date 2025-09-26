package com.example.tagriculture

import android.net.Uri
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputEditText
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.activity.viewModels
import com.example.tagriculture.viewmodels.AnimalDetailViewModel
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.example.tagriculture.data.database.WeightEntry
import com.google.android.material.card.MaterialCardView
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import java.io.File
import java.io.FileOutputStream

class AnimalDetailActivity : AppCompatActivity() {

    private var nfcTagId: String? = null
    private var animalId: Long? = null

    // UI elements
    private lateinit var toolbar: Toolbar
    private lateinit var animalTypeSpinner: AutoCompleteTextView
    private val viewModel: AnimalDetailViewModel by viewModels()
    private var selectedBirthDate: Long? = null
    private var selectedImageUri: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_animal_detail)

        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        animalTypeSpinner = findViewById(R.id.animal_type_spinner)
        val animalImageView: ImageView = findViewById(R.id.animal_image)
        val addPhotoText: TextView = findViewById(R.id.text_add_photo)

        val imageClickListener = View.OnClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        animalImageView.setOnClickListener(imageClickListener)
        addPhotoText.setOnClickListener(imageClickListener)

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
            viewModel.getWeightHistory(animalId!!).observe(this, { weightHistory ->
                if (weightHistory.isNotEmpty()) {
                    setupWeightChart(weightHistory)
                }
            })

            title = "View / Edit Animal"
            Log.d("AnimalDetail", "Editing mode for animal ID: $animalId")
            viewModel.loadAnimalDetails(animalId!!)
            observeAnimalDetails()
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

    private val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            Log.d("PhotoPicker", "Selected URI: $uri")

            val permanentUri = saveImageToInternalStorage(uri)
            selectedImageUri = permanentUri

            val animalImageView: ImageView = findViewById(R.id.animal_image)
            animalImageView.setImageURI(Uri.parse(permanentUri))

        } else {
            Log.d("PhotoPicker", "No media selected")
        }
    }

    private fun saveImageToInternalStorage(uri: Uri): String? {
        try {
            val inputStream = contentResolver.openInputStream(uri)

            val file = File(filesDir, "animal_${System.currentTimeMillis()}.jpg")

            val outputStream = FileOutputStream(file)

            inputStream?.copyTo(outputStream)

            inputStream?.close()
            outputStream.close()

            return file.absolutePath
        } catch (e: Exception) {
            Log.e("SaveImage", "Error saving image", e)
            Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show()
            return null
        }
    }

    private fun observeAnimalDetails() {
        viewModel.animalDetails.observe(this, { animal ->
            animal?.let {
                val nameEditText: TextInputEditText = findViewById(R.id.edit_text_name)
                val breedEditText: TextInputEditText = findViewById(R.id.edit_text_breed)
                val birthDateEditText: TextInputEditText = findViewById(R.id.edit_text_birth_date)
                val birthWeightEditText: TextInputEditText = findViewById(R.id.edit_text_birth_weight)
                val currentWeightEditText: TextInputEditText = findViewById(R.id.edit_text_current_weight)
                val healthAlertCard: MaterialCardView = findViewById(R.id.health_alert_card)
                viewModel.healthAlert.observe(this, { showAlert ->
                    healthAlertCard.visibility = if (showAlert) View.VISIBLE else View.GONE
                })
                val marketValueTextView: TextView = findViewById(R.id.text_market_value)
                val marketValuePHP = it.currentWeight * 150.00
                val marketValueUSD = marketValuePHP / 58.00
                val phpString = String.format(Locale.US, "₱%,.2f", marketValuePHP)
                val usdString = String.format(Locale.US, "($%,.2f USD)", marketValueUSD)
                val fullText = "$phpString $usdString"
                val spannable = SpannableString(fullText)
                spannable.setSpan(
                    ForegroundColorSpan(getColor(R.color.md_theme_onSurfaceVariant)),
                    phpString.length + 1,
                    fullText.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                val animalImageView: ImageView = findViewById(R.id.animal_image)
                if (it.pictureUri != null) {
                    val imageFile = File(it.pictureUri!!)
                    if (imageFile.exists()) {
                        animalImageView.setImageURI(Uri.fromFile(imageFile))
                        selectedImageUri = it.pictureUri
                    }
                }

                marketValueTextView.text = spannable

                currentWeightEditText.setText(it.currentWeight.toString())
                nameEditText.setText(it.name)
                breedEditText.setText(it.breed)
                animalTypeSpinner.setText(it.animalType, false)
                birthWeightEditText.setText(it.birthWeight.toString())

                selectedBirthDate = it.birthDate
                val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                birthDateEditText.setText(simpleDateFormat.format(Date(it.birthDate)))

                // TODO: Load the animal's image
            }
        })
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
        val currentWeightEditText: TextInputEditText = findViewById(R.id.edit_text_current_weight)
        val currentWeight = currentWeightEditText.text.toString().toDoubleOrNull() ?: 0.0

        if (animalId != null) {
            viewModel.animalDetails.value?.let { existingAnimal ->
                viewModel.updateAnimal(
                    animalToUpdate = existingAnimal,
                    newType = type,
                    newName = name,
                    newBreed = breed,
                    newBirthDate = selectedBirthDate!!,
                    newCurrentWeight = currentWeight,
                    newPictureUri = selectedImageUri ?: existingAnimal.pictureUri
                )
                Toast.makeText(this, "$name's details have been updated!", Toast.LENGTH_LONG).show()
            }
        } else {
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
                birthWeight = birthWeight,
                pictureUri = selectedImageUri
            )

            Toast.makeText(this, "$name has been registered!", Toast.LENGTH_LONG).show()
        }
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

    private fun setupWeightChart(history: List<WeightEntry>) {
        val lineChart: LineChart = findViewById(R.id.weight_chart)

        val entries = ArrayList<Entry>()
        history.forEach {
            entries.add(Entry(it.date.toFloat(), it.weight.toFloat()))
        }

        val dataSet = LineDataSet(entries, "Weight (kg)").apply {
            color = getColor(R.color.brand_green)
            valueTextColor = getColor(R.color.md_theme_onSurface)
            setCircleColor(getColor(R.color.md_theme_primary))
            circleHoleColor = getColor(R.color.md_theme_primary)
            lineWidth = 2f
            circleRadius = 4f
            valueTextSize = 10f
        }

        val lineData = LineData(dataSet)

        lineChart.apply {
            data = lineData
            description.isEnabled = false
            legend.isEnabled = false
            axisRight.isEnabled = false
            xAxis.textColor = getColor(R.color.md_theme_onSurfaceVariant)
            axisLeft.textColor = getColor(R.color.md_theme_onSurfaceVariant)
            invalidate()
        }
    }
}