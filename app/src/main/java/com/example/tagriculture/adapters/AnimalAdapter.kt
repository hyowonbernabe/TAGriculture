package com.example.tagriculture.adapters

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.RecyclerView
import com.example.tagriculture.R
import com.example.tagriculture.data.database.Animal
import com.google.android.material.textfield.TextInputEditText
import java.io.File

class AnimalAdapter(
    private val onAnimalClicked: (Animal) -> Unit,
    private val onSearchQueryChanged: (String) -> Unit,
    private val onFilterClicked: (View) -> Unit,
    private val onSortClicked: (View) -> Unit,
    private val onCalendarClicked: (View) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var animalList = emptyList<Animal>()

    companion object {
        private const val VIEW_TYPE_HEADER = 0
        private const val VIEW_TYPE_ANIMAL = 1
    }

    inner class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val searchInput: TextInputEditText = itemView.findViewById(R.id.search_input)
        private val filterButton: ImageButton = itemView.findViewById(R.id.btn_filter)
        private val sortButton: ImageButton = itemView.findViewById(R.id.btn_sort)
        private val calendarButton: ImageButton = itemView.findViewById(R.id.btn_calendar)

        fun bind() {
            searchInput.addTextChangedListener { text ->
                onSearchQueryChanged(text.toString())
            }
            filterButton.setOnClickListener { onFilterClicked(it) }
            sortButton.setOnClickListener { onSortClicked(it) }
            calendarButton.setOnClickListener { onCalendarClicked(it) }
        }
    }

    inner class AnimalViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val animalImage: ImageView = itemView.findViewById(R.id.animal_image)
        private val animalName: TextView = itemView.findViewById(R.id.animal_name)
        private val animalInfo: TextView = itemView.findViewById(R.id.animal_info)
        private val animalWeight: TextView = itemView.findViewById(R.id.animal_weight)

        fun bind(animal: Animal) {
            animalName.text = animal.name
            animalInfo.text = "${animal.animalType}, ${animal.breed}"
            animalWeight.text = "${animal.currentWeight} kg"
            itemView.setOnClickListener { onAnimalClicked(animal) }

            if (!animal.pictureUri.isNullOrEmpty()) {
                val uri = Uri.parse(animal.pictureUri)
                if (uri.scheme == "android.resource") {
                    animalImage.setImageURI(uri)
                } else {
                    val imageFile = File(animal.pictureUri!!)
                    if (imageFile.exists()) animalImage.setImageURI(Uri.fromFile(imageFile))
                    else animalImage.setImageResource(R.drawable.ic_launcher_foreground)
                }
            } else {
                animalImage.setImageResource(R.drawable.ic_launcher_foreground)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) VIEW_TYPE_HEADER else VIEW_TYPE_ANIMAL
    }

    override fun getItemCount(): Int {
        return animalList.size + 1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_HEADER) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_search_header, parent, false)
            HeaderViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_animal_card, parent, false)
            AnimalViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is HeaderViewHolder) {
            holder.bind()
        } else if (holder is AnimalViewHolder) {
            val animal = animalList[position - 1]
            holder.bind(animal)
        }
    }

    fun setData(animals: List<Animal>) {
        this.animalList = animals
        notifyDataSetChanged()
    }
}