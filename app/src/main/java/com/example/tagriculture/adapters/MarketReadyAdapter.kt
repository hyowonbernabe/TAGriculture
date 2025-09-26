package com.example.tagriculture.adapters

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.tagriculture.R
import com.example.tagriculture.viewmodels.MarketReadyAnimal
import java.io.File

class MarketReadyAdapter : RecyclerView.Adapter<MarketReadyAdapter.ViewHolder>() {
    private var list = emptyList<MarketReadyAnimal>()

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.animal_image)
        private val nameView: TextView = itemView.findViewById(R.id.animal_name)
        private val dateView: TextView = itemView.findViewById(R.id.ready_date_text)

        fun bind(item: MarketReadyAnimal) {
            nameView.text = item.animal.name
            dateView.text = item.formattedDate
            // Image loading logic
            if (!item.animal.pictureUri.isNullOrEmpty()) {
                val uri = Uri.parse(item.animal.pictureUri)
                if (uri.scheme == "android.resource") {
                    imageView.setImageURI(uri)
                } else {
                    val imageFile = File(item.animal.pictureUri!!)
                    if (imageFile.exists()) imageView.setImageURI(Uri.fromFile(imageFile))
                    else imageView.setImageResource(R.drawable.ic_launcher_foreground)
                }
            } else {
                imageView.setImageResource(R.drawable.ic_launcher_foreground)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_market_ready, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount() = list.size

    fun setData(newList: List<MarketReadyAnimal>) {
        this.list = newList
        notifyDataSetChanged()
    }
}