package dk.sierrasoftware.nfcscanner.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import dk.sierrasoftware.nfcscanner.MainActivity
import dk.sierrasoftware.nfcscanner.api.ApiClient
import dk.sierrasoftware.nfcscanner.api.EntityClosureDTO
import dk.sierrasoftware.nfcscanner.api.PatchEntityDTO
import dk.sierrasoftware.nfcscanner.databinding.FragmentHomeBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Collections

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val args: HomeFragmentArgs by navArgs()

    private lateinit var homeViewModel: HomeViewModel;


    private lateinit var assignParentDialog: AlertDialog


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Bind view model
        homeViewModel.entityClosure.observe(viewLifecycleOwner) {
            renderEntity(it)
        }

        setupAssignParentDialog(root)


        // Handle data
        if (args.tagUid.isNotEmpty()) {
            handleNfcTagUid(args.tagUid)
        }

        return root
    }



    private fun setupAssignParentDialog(view: View) {
        val builder = AlertDialog.Builder(view.context)
        builder.setTitle("NFC Scan")
        builder.setMessage("Approach an NFC tag to assign.")

        // Set id to signal assignment on next NFC scan
        // Remove it when dialog is dismissed, to remove assignment signal
        builder.setOnDismissListener {
            (activity as MainActivity).assignParentOnEntityIdIsActive = null;
        }

        assignParentDialog = builder.create();

        binding.assignButton.setOnClickListener {
            showAssignParentPopup()
        }
    }

    private fun renderEntity(entityClosure: EntityClosureDTO?) {
        val entity = entityClosure?.entity
        binding.tagIdValue.text = entity?.tag_uid
        binding.nameValue.text = entity?.name
        binding.parentValue.text = entityClosure?.parent?.name
        binding.assignButton.isEnabled = entityClosure?.let { true } ?: false

        for (child in entityClosure?.children ?: Collections.emptyList()) {
            val textView = TextView(context)
            textView.text = child.name
            textView.setPadding(0, 10, 0, 10)  // Add padding if needed

            // Add the TextView to the LinearLayout
            binding.childrenLayout.addView(textView)
        }
    }

    private fun handleNfcTagUid(tagUid: String) {
        val call = ApiClient.apiService.getEntitiesByTagUid(tagUid)

        call.enqueue(object : Callback<EntityClosureDTO> {
            override fun onResponse(
                call: Call<EntityClosureDTO>,
                response: Response<EntityClosureDTO>
            ) {
                if (!response.isSuccessful) {
                    val msg = String.format("Error: ${response.code()} ${response.message()}")
                    Log.e("API_ERROR", "Failure: ${msg}")
                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
                    return
                }

                val entityClosure = response.body()
                if (entityClosure == null) {
                    Toast.makeText(context, "Null", Toast.LENGTH_SHORT).show();
                    return
                }

                // If assign in progress, assign entity to tag
                (activity as MainActivity).assignParentOnEntityIdIsActive?.let {
                    assignParentDialog.dismiss()
                    assignEntityToTag(it, entityClosure.entity.id)
                    return
                }

                homeViewModel.entityClosure.value = entityClosure
            }

            override fun onFailure(call: Call<EntityClosureDTO>, t: Throwable) {
                // TODO: Add option to create new entity
                Log.e("API_ERROR", "Failure: ${t.message}")
                Toast.makeText(context, t.message, Toast.LENGTH_LONG).show()
                return
            }
        })
    }

    private fun showAssignParentPopup() {
        val entityId = homeViewModel.entityClosure.value!!.entity.id
        (activity as MainActivity).assignParentOnEntityIdIsActive = entityId;
        assignParentDialog.show()
    }

    private fun assignEntityToTag(entryId: UInt, newParentId: UInt) {
        val patchEntity = PatchEntityDTO(newParentId)
        val call = ApiClient.apiService.patchEntity(entryId, patchEntity)

        call.enqueue(object : Callback<EntityClosureDTO> {
            override fun onResponse(call: Call<EntityClosureDTO>, response: Response<EntityClosureDTO>) {
                if (!response.isSuccessful) {
                    // Handle error
                    val msg = String.format("Error: ${response.code()} ${response.message()}")
                    Log.e("API_ERROR", msg)
                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
                    return
                }

                val entityClosure = response.body()
                if (entityClosure == null) {
                    Toast.makeText(context, "Null", Toast.LENGTH_SHORT).show();
                    return
                }

                homeViewModel.entityClosure.value = entityClosure
            }

            override fun onFailure(call: Call<EntityClosureDTO>, t: Throwable) {
                // Handle failure
                Log.e("API_ERROR", "Failure: ${t.message}")
                Toast.makeText(context, t.message, Toast.LENGTH_LONG).show()
                return
            }
        })
    }
}