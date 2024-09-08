package dk.sierrasoftware.nfcscanner

import android.app.AlertDialog
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import dk.sierrasoftware.nfcscanner.api.CheckForUpdateDTO
import dk.sierrasoftware.nfcscanner.api.CreateEntityDTO
import dk.sierrasoftware.nfcscanner.api.EntityClient
import dk.sierrasoftware.nfcscanner.api.UtilClient
import dk.sierrasoftware.nfcscanner.databinding.ActivityMainBinding
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    // NFC
    private lateinit var nfcAdapter: NfcAdapter
    private lateinit var pendingIntent: PendingIntent
    private lateinit var intentFilters: Array<IntentFilter>

    public var assignParentOnEntityIdIsActive: Int? = null // Signal to assign entity to tag on next NFC scan

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
        val actionNavigationHome = MobileNavigationDirections.actionNavigationHomeWithTagUid(tagUid)
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
    private suspend fun checkForUpdate() {
        val versionName = packageManager.getPackageInfo(packageName, 0).versionName
        // Check format is "vX.Y.Z-alpha.1+build.1234"
        assert(versionName.contains(Regex("^([0-9]+)\\.([0-9]+)\\.([0-9]+)$")))

        UtilClient.client.checkForUpdate(CheckForUpdateDTO(versionName)).onSuccess { body ->
            val mandatory = body.update_mandatory != false
            val recommended = body.update_recommended == true

            if (mandatory || recommended) {
                AlertDialog.Builder(this@MainActivity)
                    // "Update available", or "Update mandatory"
                    .setTitle(body.title ?: "Update ${if (mandatory) "mandatory" else "available"}")
                    .setMessage(body.message ?: "Please update the app to the latest version")
                    .setPositiveButton("Update") { _, _ ->
                        // Go to url
                        body.update_url?.let {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(it))
                            startActivity(intent)
                        }

                        // Close the app if the update is mandatory
                        if (mandatory) {
                            finish()
                        }
                    }
                    .setNegativeButton("Cancel") { dialog, _ ->
                        dialog.cancel()
                        if (mandatory) {
                            finish()
                        }
                    }
                    .setCancelable(!mandatory)
                    .show()
            }
        }
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
            R.id.action_create_entity -> {
                lifecycleScope.launch {
                    createNewEntity()
                }
                true
            }
            R.id.action_reset_tag -> {
                Toast.makeText(this, "Todo reset", Toast.LENGTH_LONG).show()
                true
            }
            R.id.action_delete_entity -> {
                // Show on home screen?
                Toast.makeText(this, "Todo delete", Toast.LENGTH_LONG).show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private suspend fun createNewEntity() {
        val newEntity = CreateEntityDTO(name = "New entity", tag_uid = null, parent_id = null);
        EntityClient.client.createEntity(newEntity).onSuccess { entity ->
            val actionNavigationHome = MobileNavigationDirections.actionNavigationHomeWithEntityId(entity.id.toInt())
            val navController = Navigation.findNavController(this@MainActivity, R.id.nav_host_fragment_activity_main)
            navController.navigate(actionNavigationHome)
        }.onFailure { t ->
            Toast.makeText(this, t.message, Toast.LENGTH_LONG).show()
        }
    }
}