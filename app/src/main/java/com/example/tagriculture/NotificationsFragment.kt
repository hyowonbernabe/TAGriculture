package com.example.tagriculture

import NotificationsViewModel
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tagriculture.adapters.NotificationsAdapter

class NotificationsFragment : Fragment() {

    private val viewModel: NotificationsViewModel by viewModels()
    private val notificationsAdapter = NotificationsAdapter()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_notifications, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView: RecyclerView = view.findViewById(R.id.notifications_recycler_view)
        recyclerView.adapter = notificationsAdapter

        val layoutManager = recyclerView.layoutManager as LinearLayoutManager
        val dividerItemDecoration = DividerItemDecoration(recyclerView.context, layoutManager.orientation)
        recyclerView.addItemDecoration(dividerItemDecoration)

        viewModel.allNotifications.observe(viewLifecycleOwner) { notifications ->
            notificationsAdapter.setData(notifications)
        }
    }

    override fun onResume() {
        super.onResume()
    }
}