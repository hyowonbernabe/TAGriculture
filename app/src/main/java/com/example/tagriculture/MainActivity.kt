package com.example.tagriculture

import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag as NfcTag
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.tagriculture.viewmodels.MainViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Log.d("DEBUG_NFC", "MainActivity onCreate")

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        bottomNavigationView.setupWithNavController(navController)

        handleNfcIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        handleNfcIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleNfcIntent(intent)
    }

    private fun handleNfcIntent(intent: Intent?) {
        if (intent != null && (intent.hasExtra(NfcAdapter.EXTRA_TAG) ||
                    NfcAdapter.ACTION_TECH_DISCOVERED == intent.action ||
                    NfcAdapter.ACTION_TAG_DISCOVERED == intent.action)) {

            Log.d("DEBUG_NFC", "1. handleNfcIntent - Intent has NFC data. Action: ${intent.action}")

            val detectedTag: NfcTag? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra(NfcAdapter.EXTRA_TAG, NfcTag::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)
            }

            detectedTag?.let {
                val nfcSerialNumber = bytesToHexString(it.id)
                Log.d("DEBUG_NFC", "2. handleNfcIntent - Tag detected: $nfcSerialNumber")
                mainViewModel.onNfcTagScanned(nfcSerialNumber)
                Log.d("DEBUG_NFC", "3. handleNfcIntent - Called mainViewModel.onNfcTagScanned")
            }

            intent.removeExtra(NfcAdapter.EXTRA_TAG)
        }
    }

    private fun bytesToHexString(bytes: ByteArray): String {
        val stringBuilder = StringBuilder(bytes.size * 2)
        for (byte in bytes) {
            stringBuilder.append(String.format("%02X", byte))
        }
        return stringBuilder.toString()
    }
}