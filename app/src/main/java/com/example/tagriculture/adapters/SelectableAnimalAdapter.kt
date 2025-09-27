package com.example.tagriculture.adapters

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.tagriculture.R
import com.example.tagriculture.data.database.Animal
import java.io.File

class SelectableAnimalAdapter(private val onAnimalSelected: (Animal) -> Unit) :
    RecyclerView.Adapter<SelectableAnimalAdapter.ViewHolder>() {

    private var animalList = emptyList<Animal>()

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.animal_image)
        private val nameView: TextView = itemView.findViewById(R.id.animal_name)
        private val infoView: TextView = itemView.findViewById(R.id.animal_info)

        fun bind(animal: Animal, onAnimalSelected: (Animal) -> Unit) {
            nameView.text = animal.name
            infoView.text = "${animal.animalType}, ${animal.breed}"
            itemView.setOnClickListener { onAnimalSelected(animal) }

            if (!animal.pictureUri.isNullOrEmpty()) {
                val uri = Uri.parse(animal.pictureUri)
                if (uri.scheme == "android.resource") {
                    imageView.setImageURI(uri)
                } else {
                    val imageFile = File(animal.pictureUri!!)
                    if (imageFile.exists()) imageView.setImageURI(Uri.fromFile(imageFile))
                    else imageView.setImageResource(R.drawable.ic_launcher_foreground)
                }
            } else {
                imageView.setImageResource(R.drawable.ic_launcher_foreground)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_animal_selectable, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(animalList[position], onAnimalSelected)
    }

    override fun getItemCount() = animalList.size

    fun setData(newList: List<Animal>) {
        this.animalList = newList
        notifyDataSetChanged()
    }
}