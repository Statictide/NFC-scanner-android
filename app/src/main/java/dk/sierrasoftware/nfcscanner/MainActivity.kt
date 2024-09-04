package dk.sierrasoftware.nfcscanner

import android.app.AlertDialog
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import dk.sierrasoftware.nfcscanner.api.ApiClient.apiService
import dk.sierrasoftware.nfcscanner.api.CheckForUpdateDTO
import dk.sierrasoftware.nfcscanner.api.CheckForUpdateResponseDTO
import dk.sierrasoftware.nfcscanner.databinding.ActivityMainBinding
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    // NFC
    private lateinit var nfcAdapter: NfcAdapter
    private lateinit var pendingIntent: PendingIntent
    private lateinit var intentFilters: Array<IntentFilter>

    public var assignParentOnEntityIdIsActive: UInt? = null // Signal to assign entity to tag on next NFC scan

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // UI
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(setOf(R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications))
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        // NFC
        setupNFC()

        // Call the check-for-update endpoint on startup
        lifecycleScope.launch {
            checkForUpdate()
        }
    }

    private fun setupNFC() {
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)

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

        // Get tag uid
        val tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG, Tag::class.java)!!
        val tagUid: String = bytesToHex(tag.id)

        // Navigate
        val actionNavigationHome = MobileNavigationDirections.actionNavigationHomeWithTagUid(tagUid, 0)
        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        navController.navigate(actionNavigationHome)
    }

    // Helper method to convert byte array to hexadecimal string
    private fun bytesToHex(bytes: ByteArray): String {
        val sb = StringBuilder()
        for (b in bytes) {
            sb.append(String.format("%02X", b))
        }
        return sb.toString()
    }

    // Show popup if the app needs an update.
    private fun checkForUpdate() {
        val versionName = packageManager.getPackageInfo(packageName, 0).versionName
        // Check format is "vX.Y.Z-alpha.1+build.1234"
        assert(versionName.contains(Regex("^([0-9]+)\\.([0-9]+)\\.([0-9]+)$")))

        val call = apiService.checkForUpdate(CheckForUpdateDTO(versionName))
        call.enqueue(object : Callback<CheckForUpdateResponseDTO> {
            override fun onResponse(
                call: Call<CheckForUpdateResponseDTO>,
                response: Response<CheckForUpdateResponseDTO>
            ) {
                if (!response.isSuccessful) {
                    val msg = String.format("Error: ${response.code()} ${response.message()}")
                    Log.e("API_ERROR", "Failure: ${msg}")
                    Toast.makeText(this@MainActivity, msg, Toast.LENGTH_LONG).show();
                    return
                }

                val body = response.body()!!

                if (body.update_required || body.update_recomended == true) {
                    AlertDialog.Builder(this@MainActivity)
                    .setTitle("Update Required")
                    .setMessage(body.message ?: "Please update the app to the latest version")
                    .setPositiveButton("Ok") { _, _ ->
                        // Redirect to the Play Store or any URL
                        // val intent = Intent(Intent.ACTION_VIEW, Uri.parse("www.example.com/nfc-scanner"))
                        // startActivity(intent)
                    }
                    .setNegativeButton("Cancel") { dialog, _ ->
                        dialog.dismiss()
                        if (body.update_required) {
                            // Optionally, close the app if the update is mandatory
                        }
                        finish()
                    }
                    .setCancelable(!body.update_required)
                    .show()
                }
            }

            override fun onFailure(call: Call<CheckForUpdateResponseDTO>, t: Throwable) {
                Log.e("API_ERROR", "Failure: ${t.message}")
                return
            }
        })
    }


    // Menu
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection.
        return when (item.itemId) {
            R.id.action_create_item -> {
                Toast.makeText(this, "Todo create", Toast.LENGTH_LONG)
                true
            }
            R.id.action_reset_tag -> {
                Toast.makeText(this, "Todo reset", Toast.LENGTH_LONG)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}