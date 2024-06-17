package dk.sierrasoftware.nfcscanner

import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import dk.sierrasoftware.nfcscanner.api.ApiClient
import dk.sierrasoftware.nfcscanner.api.EntityClosureDTO
import dk.sierrasoftware.nfcscanner.databinding.ActivityMainBinding
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

        // NFC
        setupNFC()
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

        val tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG, Tag::class.java)!!
        val tagId = tag.id
        val tagIdHex: String = bytesToHex(tagId)

        val call = ApiClient.apiService.getEntitiesByTagUid(tagIdHex)

        val context = this
        call.enqueue(object : Callback<EntityClosureDTO> {
            override fun onResponse(call: Call<EntityClosureDTO>, response: Response<EntityClosureDTO>) {
                if (!response.isSuccessful) {
                    // Handle error
                    val msg = String.format("Error: ${response.code()} ${response.message()}")
                    Log.e("API_ERROR", msg)
                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
                    return
                }

                val entity = response.body()
                if (entity == null) {
                    Toast.makeText(context, "Null", Toast.LENGTH_SHORT).show();
                    return
                }

                // Delegate to active fragment
                val navController = findNavController(R.id.nav_host_fragment_activity_main)
                navController.navigateUp()
                val bundle = bundleOf("tag_uid" to entity.entity.tag_uid, "name" to entity.entity.name)
                navController.navigate(R.id.navigation_home, bundle)
            }

            override fun onFailure(call: Call<EntityClosureDTO>, t: Throwable) {
                // TODO: Add option to create new entity
                // Handle failure
                Log.e("API_ERROR", "Failure: ${t.message}")
                Toast.makeText(context, t.message, Toast.LENGTH_LONG).show()
                return
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


    fun assignEntityTo(view: View) {
        Toast.makeText(this, "Scan nfc tag to assign", Toast.LENGTH_SHORT).show();
    }
}