package com.example.myapplication

import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.myapplication.api.ApiClient
import com.example.myapplication.api.Entity
import com.example.myapplication.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    // NFC
    private lateinit var nfcAdapter: NfcAdapter
    private lateinit var pendingIntent: PendingIntent
    private lateinit var intentFilters: Array<IntentFilter>

    // Views
    private lateinit var tagIdTextView: TextView
    private lateinit var nameTextView: TextView
    private lateinit var ownerTextView: TextView
    private lateinit var tagImageView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // UI
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(setOf(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications))
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        // Extracting views
        tagIdTextView = findViewById(R.id.tag_id_value);
        nameTextView = findViewById(R.id.name_value);
        ownerTextView = findViewById(R.id.owner_value);
        tagImageView = findViewById(R.id.tag_image);

        // NFC
        setupNFC();

    }

    private fun setupNFC() {
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)

        if (nfcAdapter == null) {
            Toast.makeText(this, "NFC is not available on this device", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        if (!nfcAdapter.isEnabled) {
            Toast.makeText(this, "NFC is not enabled", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        pendingIntent = PendingIntent.getActivity(
            this, 0, Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), PendingIntent.FLAG_MUTABLE
        )

        intentFilters = arrayOf(IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED));
    }

    override fun onResume() {
        super.onResume()
        nfcAdapter.enableForegroundDispatch(this, pendingIntent, intentFilters, null)
    }

    override fun onPause() {
        super.onPause()
        nfcAdapter.disableForegroundDispatch(this)
    }

    public override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (NfcAdapter.ACTION_TAG_DISCOVERED != intent.action) {
            Toast.makeText(this, "Please erase tag", Toast.LENGTH_LONG).show()
            return
        }

        val tag = intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG)
        val tagId = tag!!.id
        val tagIdHex: String = bytesToHex(tagId)

        var call = ApiClient.apiService.getEntitiesByTagId(tagIdHex)

        tagIdTextView.text = "loading..."

        call.enqueue(object : Callback<Array<Entity>> {
            override fun onResponse(call: Call<Array<Entity>>, response: Response<Array<Entity>>) {
                if (!response.isSuccessful) {
                    // Handle error
                    Log.e("API_ERROR", "Error: ${response.code()} ${response.message()}")
                    tagIdTextView.text = String.format("Error: ${response.code()} ${response.message()}")
                    return
                }

                val entities = response.body()
                if (entities == null) {
                    tagIdTextView.text = "Null"
                    return
                }

                // Handle the retrieved post data
                if (entities.isEmpty()) {
                    tagIdTextView.text = tagIdHex
                    nameTextView.text = ""
                    ownerTextView.text = ""
                    return
                }

                val entity = entities[0]
                tagIdTextView.text = entity.tag_id
                nameTextView.text = entity.name
                ownerTextView.text = entity.owner
            }

            override fun onFailure(call: Call<Array<Entity>>, t: Throwable) {
                // Handle failure
                Log.e("API_ERROR", "Failure: ${t.message}")
                tagIdTextView.text = t.message
            }
        })
    }

    // Helper method to convert byte array to hexadecimal string
    private fun bytesToHex(bytes: ByteArray): String {
        val sb = StringBuilder()
        for (b in bytes) {
            sb.append(String.format("%02X", b))
        }
        return sb.toString()
    }
}