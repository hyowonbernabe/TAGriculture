package com.example.tagriculture

import android.app.PendingIntent
import android.content.Intent
import android.nfc.NfcAdapter
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.example.tagriculture.adapters.AnimalAdapter
import com.example.tagriculture.viewmodels.MainViewModel
import com.example.tagriculture.viewmodels.ScanViewModel

class ScanFragment : Fragment() {

    private val scanViewModel: ScanViewModel by viewModels()
    private val mainViewModel: MainViewModel by activityViewModels()

    private lateinit var recyclerView: RecyclerView
    private val animalAdapter = AnimalAdapter()

    private var nfcAdapter: NfcAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_scan, container, false)

        nfcAdapter = NfcAdapter.getDefaultAdapter(requireContext())

        recyclerView = view.findViewById(R.id.livestock_recycler_view)
        recyclerView.adapter = animalAdapter

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        scanViewModel.allAnimals.observe(viewLifecycleOwner, Observer { animals ->
            animalAdapter.setData(animals)
        })

        mainViewModel.nfcTagId.observe(viewLifecycleOwner, Observer { tagId ->
            tagId?.let {
                Log.d("NFC", "ScanFragment received tag: $it")
                Toast.makeText(requireContext(), "Scanned Tag: $it", Toast.LENGTH_LONG).show()
                mainViewModel.onNfcTagProcessed()
            }
        })
    }

    override fun onResume() {
        super.onResume()
        nfcAdapter?.let {
            val intent = Intent(requireActivity(), requireActivity().javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            val pendingIntent = PendingIntent.getActivity(requireContext(), 0, intent, PendingIntent.FLAG_IMMUTABLE)
            it.enableForegroundDispatch(requireActivity(), pendingIntent, null, null)
            Log.d("NFC", "Foreground dispatch enabled in ScanFragment")
        }
    }

    override fun onPause() {
        super.onPause()
        nfcAdapter?.disableForegroundDispatch(requireActivity())
        Log.d("NFC", "Foreground dispatch disabled in ScanFragment")
    }
}