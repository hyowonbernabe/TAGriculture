package com.example.tagriculture.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.tagriculture.R
import com.example.tagriculture.data.database.Animal
import android.net.Uri
import java.io.File

class AnimalAdapter(private val listener: (Animal) -> Unit) : RecyclerView.Adapter<AnimalAdapter.AnimalViewHolder>() {

    private var animalList = emptyList<Animal>()

    class AnimalViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val animalImage: ImageView = itemView.findViewById(R.id.animal_image)
        val animalName: TextView = itemView.findViewById(R.id.animal_name)
        val animalInfo: TextView = itemView.findViewById(R.id.animal_info)
        val animalWeight: TextView = itemView.findViewById(R.id.animal_weight)

        fun bind(animal: Animal, listener: (Animal) -> Unit) {
            itemView.setOnClickListener { listener(animal) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnimalViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_animal_card, parent, false)
        return AnimalViewHolder(view)
    }

    override fun getItemCount(): Int {
        return animalList.size
    }

    override fun onBindViewHolder(holder: AnimalViewHolder, position: Int) {
        val currentAnimal = animalList[position]

        holder.animalName.text = currentAnimal.name
        holder.animalInfo.text = "${currentAnimal.animalType}, ${currentAnimal.breed}"
        holder.animalWeight.text = "${currentAnimal.currentWeight} kg"
        if (!currentAnimal.pictureUri.isNullOrEmpty()) {
            val uriString = currentAnimal.pictureUri!!
            val uri = Uri.parse(uriString)

            // Check if the URI is a local file path or a resource URI
            if (uri.scheme == "android.resource") {
                // It's a mock data image from drawables
                holder.animalImage.setImageURI(uri)
            } else {
                val imageFile = File(uriString)
                if (imageFile.exists()) {
                    holder.animalImage.setImageURI(Uri.fromFile(imageFile))
                } else {
                    holder.animalImage.setImageResource(R.drawable.ic_launcher_foreground) // Fallback
                }
            }
        } else {
            holder.animalImage.setImageResource(R.drawable.ic_launcher_foreground) // Fallback
        }

        holder.bind(currentAnimal, listener)
    }

    fun setData(animals: List<Animal>) {
        this.animalList = animals
        notifyDataSetChanged()
    }
}