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
import com.example.tagriculture.ui.NewTagDialogFragment
import com.example.tagriculture.viewmodels.MainViewModel
import com.example.tagriculture.viewmodels.ScanResult
import com.example.tagriculture.viewmodels.ScanViewModel

class ScanFragment : Fragment(), NewTagDialogFragment.NewTagDialogListener {

    private val scanViewModel: ScanViewModel by viewModels()
    private val mainViewModel: MainViewModel by activityViewModels()
    private val animalAdapter = AnimalAdapter { selectedAnimal ->
        Log.d("GridClick", "Clicked on animal ID: ${selectedAnimal.id}")
        val intent = Intent(requireActivity(), AnimalDetailActivity::class.java).apply {
            putExtra("ANIMAL_ID", selectedAnimal.id)
        }
        startActivity(intent)
    }
    private var nfcAdapter: NfcAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_scan, container, false)
        nfcAdapter = NfcAdapter.getDefaultAdapter(requireContext())
        val recyclerView: RecyclerView = view.findViewById(R.id.livestock_recycler_view)
        recyclerView.adapter = animalAdapter
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        scanViewModel.allAnimals.observe(viewLifecycleOwner, Observer { animals ->
            animalAdapter.setData(animals)
        })

        mainViewModel.scanResult.observe(viewLifecycleOwner, Observer { result ->
            result?.let {
                when (it) {
                    is ScanResult.KnownAnimal -> {
                        Log.d("NFC", "Known animal scanned with ID: ${it.animalId}. Opening details.")
                        val intent = Intent(requireActivity(), AnimalDetailActivity::class.java).apply {
                            putExtra("ANIMAL_ID", it.animalId)
                        }
                        startActivity(intent)
                    }
                    is ScanResult.NewTag -> {
                        Log.d("NFC", "New tag detected: ${it.tagId}. Showing dialog.")
                        val dialog = NewTagDialogFragment.newInstance(it.tagId)
                        dialog.setTargetFragment(this@ScanFragment, 0)
                        dialog.show(parentFragmentManager, "NewTagDialog")
                    }
                    is ScanResult.UnassignedTag -> {
                        Log.d("NFC", "Unassigned tag detected: ${it.tagId}. Showing dialog.")
                        val dialog = NewTagDialogFragment.newInstance(it.tagId)
                        dialog.setTargetFragment(this@ScanFragment, 0)
                        dialog.show(parentFragmentManager, "NewTagDialog")
                    }
                }
                mainViewModel.onScanResultProcessed()
            }
        })
    }

    override fun onResume() {
        super.onResume()
        nfcAdapter?.let {
            val intent = Intent(requireActivity(), requireActivity().javaClass).apply {
                addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            }
            val pendingIntent = PendingIntent.getActivity(
                requireContext(),
                0,
                intent,
                PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            it.enableForegroundDispatch(requireActivity(), pendingIntent, null, null)
            Log.d("NFC", "Foreground dispatch enabled in ScanFragment")
        }
    }

    override fun onPause() {
        super.onPause()
        nfcAdapter?.disableForegroundDispatch(requireActivity())
        Log.d("NFC", "Foreground dispatch disabled in ScanFragment")
    }

    override fun onRegisterClicked(tagId: String) {
        Log.d("NFC", "Register button clicked for tag: $tagId")
        val intent = Intent(requireActivity(), AnimalDetailActivity::class.java).apply {
            putExtra("NFC_TAG_ID", tagId)
        }
        startActivity(intent)
    }
}