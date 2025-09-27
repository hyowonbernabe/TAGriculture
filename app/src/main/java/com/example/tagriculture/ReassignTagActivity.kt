package com.example.tagriculture

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tagriculture.adapters.SelectableAnimalAdapter
import com.example.tagriculture.viewmodels.ReassignTagViewModel

class ReassignTagActivity : AppCompatActivity() {

    private val viewModel: ReassignTagViewModel by viewModels()
    private var nfcTagId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reassign_tag)

        nfcTagId = intent.getStringExtra("NFC_TAG_ID")
        if (nfcTagId == null) {
            Toast.makeText(this, "Error: NFC Tag ID missing", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        val selectableAnimalAdapter = SelectableAnimalAdapter { selectedAnimal ->
            viewModel.reassignTag(nfcTagId!!, selectedAnimal.id)
            Toast.makeText(this, "${selectedAnimal.name} is now assigned to the new tag.", Toast.LENGTH_LONG).show()
            finish()
        }

        val recyclerView: RecyclerView = findViewById(R.id.animal_list_recycler_view)
        recyclerView.adapter = selectableAnimalAdapter
        recyclerView.addItemDecoration(DividerItemDecoration(this, (recyclerView.layoutManager as LinearLayoutManager).orientation))

        viewModel.allAnimals.observe(this) { animals ->
            selectableAnimalAdapter.setData(animals)
        }
    }
}