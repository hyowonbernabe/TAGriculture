package com.example.tagriculture

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.tagriculture.adapters.MarketReadyAdapter
import com.example.tagriculture.viewmodels.HomeViewModel

class SalesCalendarFragment : Fragment() {

    private val viewModel: HomeViewModel by viewModels()
    private val marketReadyAdapter = MarketReadyAdapter()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_sales_calendar, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val toolbar: Toolbar = view.findViewById(R.id.toolbar)
        toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        val recyclerView: RecyclerView = view.findViewById(R.id.market_ready_recycler_view)
        recyclerView.adapter = marketReadyAdapter

        viewModel.marketReadyList.observe(viewLifecycleOwner) { list ->
            marketReadyAdapter.setData(list)
        }
    }
}