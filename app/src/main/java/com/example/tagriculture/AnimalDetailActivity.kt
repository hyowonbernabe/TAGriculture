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
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.tagriculture.data.database.WeightEntry
import com.example.tagriculture.viewmodels.AnimalDetailViewModel
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.google.android.material.card.MaterialCardView
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputEditText
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AnimalDetailActivity : AppCompatActivity() {

    private val viewModel: AnimalDetailViewModel by viewModels()

    private var nfcTagId: String? = null
    private var animalId: Long? = null
    private var selectedBirthDate: Long? = null
    private var selectedImageUri: String? = null

    private lateinit var toolbar: Toolbar
    private lateinit var animalTypeSpinner: AutoCompleteTextView
    private lateinit var animalImageView: ImageView
    private lateinit var addPhotoText: TextView
    private lateinit var nameEditText: TextInputEditText
    private lateinit var breedEditText: TextInputEditText
    private lateinit var birthDateEditText: TextInputEditText
    private lateinit var birthWeightEditText: TextInputEditText
    private lateinit var currentWeightEditText: TextInputEditText

    private val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            Log.d("PhotoPicker", "Selected URI: $uri")
            val permanentUri = saveImageToInternalStorage(uri)
            selectedImageUri = permanentUri
            animalImageView.setImageURI(Uri.parse(permanentUri))
        } else {
            Log.d("PhotoPicker", "No media selected")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_animal_detail)

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        initializeViews()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.detail_container)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(v.paddingLeft, v.paddingTop, v.paddingRight, systemBars.bottom)
            insets
        }

        nfcTagId = intent.getStringExtra("NFC_TAG_ID")
        val receivedAnimalId = intent.getLongExtra("ANIMAL_ID", -1L)
        if (receivedAnimalId != -1L) {
            animalId = receivedAnimalId
        }

        if (animalId != null) {
            title = "View / Edit Animal"
            viewModel.loadAnimal(animalId!!)
            setupObservers()
        } else if (nfcTagId != null) {
            title = "Register New Animal"
        } else {
            title = "Error"
            Toast.makeText(this, "No animal or tag ID provided", Toast.LENGTH_LONG).show()
            finish()
        }

        setupAnimalTypeSpinner()
        setupClickListeners()
    }

    private fun initializeViews() {
        animalTypeSpinner = findViewById(R.id.animal_type_spinner)
        animalImageView = findViewById(R.id.animal_image)
        addPhotoText = findViewById(R.id.text_add_photo)
        nameEditText = findViewById(R.id.edit_text_name)
        breedEditText = findViewById(R.id.edit_text_breed)
        birthDateEditText = findViewById(R.id.edit_text_birth_date)
        birthWeightEditText = findViewById(R.id.edit_text_birth_weight)
        currentWeightEditText = findViewById(R.id.edit_text_current_weight)
    }

    private fun setupClickListeners() {
        birthDateEditText.setOnClickListener { showDatePicker() }
        val imageClickListener = View.OnClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
        animalImageView.setOnClickListener(imageClickListener)
        addPhotoText.setOnClickListener(imageClickListener)
    }

    private fun setupObservers() {
        viewModel.animalDetails.observe(this) { animal ->
            animal?.let {
                nameEditText.setText(it.name)
                breedEditText.setText(it.breed)
                animalTypeSpinner.setText(it.animalType, false)
                birthWeightEditText.setText(it.birthWeight.toString())
                currentWeightEditText.setText(it.currentWeight.toString())

                selectedBirthDate = it.birthDate
                val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                birthDateEditText.setText(simpleDateFormat.format(Date(it.birthDate)))

                if (it.pictureUri != null) {
                    val uriString = it.pictureUri!!
                    val uri = Uri.parse(uriString)
                    selectedImageUri = uriString
                    if (uri.scheme == "android.resource") {
                        animalImageView.setImageURI(uri)
                    } else {
                        val imageFile = File(uriString)
                        if (imageFile.exists()) {
                            animalImageView.setImageURI(Uri.fromFile(imageFile))
                        }
                    }
                }

                val marketValueTextView: TextView = findViewById(R.id.text_market_value)
                val marketValuePHP = it.currentWeight * 150.00
                val marketValueUSD = marketValuePHP / 58.00
                val phpString = String.format(Locale.US, "₱%,.2f", marketValuePHP)
                val usdString = String.format(Locale.US, "($%,.2f USD)", marketValueUSD)
                val fullText = "$phpString $usdString"
                val spannable = SpannableString(fullText)
                spannable.setSpan(
                    ForegroundColorSpan(getColor(R.color.md_theme_onSurfaceVariant)),
                    phpString.length + 1, fullText.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                marketValueTextView.text = spannable
            }
        }

        viewModel.weightHistory.observe(this) { history ->
            if (!history.isNullOrEmpty()) {
                setupWeightChart(history)
            }
        }

        val healthAlertCard: MaterialCardView = findViewById(R.id.health_alert_card)
        viewModel.healthAlert.observe(this) { showAlert ->
            healthAlertCard.visibility = if (showAlert) View.VISIBLE else View.GONE
        }

        val feiTextView: TextView = findViewById(R.id.text_fei)
        val conditionTextView: TextView = findViewById(R.id.text_condition_score)
        val readinessCard: MaterialCardView = findViewById(R.id.readiness_alert_card)
        val readinessText: TextView = findViewById(R.id.readiness_alert_text)
        viewModel.analyticsReport.observe(this) { report ->
            report?.let {
                feiTextView.text = String.format(Locale.US, "%.2f", it.feedEfficiencyIndex)
                conditionTextView.text = it.conditionScore
                if (it.readinessAlerts.isNotEmpty()) {
                    readinessText.text = "Alert: " + it.readinessAlerts.joinToString(", ")
                    readinessCard.visibility = View.VISIBLE
                } else {
                    readinessCard.visibility = View.GONE
                }
            }
        }
    }

    private fun setupAnimalTypeSpinner() {
        val animalTypes = listOf("Cattle", "Pig", "Sheep", "Goat", "Horse", "Buffalo")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, animalTypes)
        animalTypeSpinner.setAdapter(adapter)
    }

    private fun showDatePicker() {
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select Birth Date")
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
            .build()
        datePicker.addOnPositiveButtonClickListener { selection ->
            selectedBirthDate = selection
            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            birthDateEditText.setText(simpleDateFormat.format(Date(selection)))
        }
        datePicker.show(supportFragmentManager, "DATE_PICKER_TAG")
    }

    private fun saveAnimal() {
        val type = animalTypeSpinner.text.toString()
        val name = nameEditText.text.toString()
        val breed = breedEditText.text.toString()
        val birthWeight = birthWeightEditText.text.toString().toDoubleOrNull() ?: 0.0
        val currentWeight = currentWeightEditText.text.toString().toDoubleOrNull() ?: 0.0

        if (name.isBlank() || type.isBlank()) {
            Toast.makeText(this, "Please fill in Animal Type and Name", Toast.LENGTH_SHORT).show()
            return
        }
        if (selectedBirthDate == null) {
            Toast.makeText(this, "Please select a Birth Date", Toast.LENGTH_SHORT).show()
            return
        }

        if (animalId != null) {
            viewModel.animalDetails.value?.let { existingAnimal ->
                viewModel.updateAnimal(
                    animalToUpdate = existingAnimal,
                    newType = type,
                    newName = name,
                    newBreed = breed,
                    newBirthDate = selectedBirthDate!!,
                    newCurrentWeight = currentWeight,
                    newPictureUri = selectedImageUri
                )
                Toast.makeText(this, "$name's details have been updated!", Toast.LENGTH_LONG).show()
            }
        } else {
            if (nfcTagId == null) {
                Toast.makeText(this, "Error: NFC Tag ID is missing", Toast.LENGTH_SHORT).show()
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
}