package com.example.tagriculture.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.tagriculture.R
import com.example.tagriculture.data.database.Notification

class NotificationsAdapter : RecyclerView.Adapter<NotificationsAdapter.NotificationViewHolder>() {

    private var notificationList = emptyList<Notification>()

    class NotificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val colorBar: View = itemView.findViewById(R.id.color_bar)
        private val titleView: TextView = itemView.findViewById(R.id.notification_title)
        private val messageView: TextView = itemView.findViewById(R.id.notification_message)

        fun bind(notification: Notification) {
            titleView.text = notification.animalName
            messageView.text = notification.message

            val colorRes = when (notification.alertType) {
                "HEALTH" -> R.color.chart_red
                "MARKET" -> R.color.brand_green
                "BREEDING" -> R.color.alert_pink
                else -> R.color.md_theme_outline
            }

            colorBar.setBackgroundColor(ContextCompat.getColor(itemView.context, colorRes))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notification, parent, false)
        return NotificationViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        holder.bind(notificationList[position])
    }

    override fun getItemCount(): Int {
        return notificationList.size
    }

    fun setData(notifications: List<Notification>) {
        this.notificationList = notifications
        notifyDataSetChanged()
    }
}