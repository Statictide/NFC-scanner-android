package dk.sierrasoftware.nfcscanner.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import dk.sierrasoftware.nfcscanner.MainActivity
import dk.sierrasoftware.nfcscanner.api.ApiClient
import dk.sierrasoftware.nfcscanner.api.EnrichedEntityDTO
import dk.sierrasoftware.nfcscanner.api.PatchEntityDTO
import dk.sierrasoftware.nfcscanner.databinding.FragmentHomeBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val args: HomeFragmentArgs by navArgs()

    private lateinit var homeViewModel: HomeViewModel;

    private lateinit var entityAdapter: EntityAdapter


    private lateinit var assignParentDialog: AlertDialog

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        entityAdapter = EntityAdapter(homeViewModel.entityClosure.value?.children.orEmpty()) // Initially empty list

        // Bind view model
        homeViewModel.entityClosure.observe(viewLifecycleOwner) {
            // Handle null
            if (it == null) {
                binding.nameValue.text = "N/A"
                binding.parentValue.text = "N/A"
                binding.assignButton.isEnabled = false
                binding.childrenRecyclerView.adapter = null
                return@observe
            }

            // Handle it
            binding.nameValue.text = it.entity.name
            binding.parentValue.text = it.parent?.name
            binding.assignButton.isEnabled = true
            entityAdapter = EntityAdapter(it.children)
            binding.childrenRecyclerView.adapter = EntityAdapter(it.children )

        }

        binding.childrenRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = entityAdapter
        }

        setupAssignParentDialog(root)

        // Fetch entity
        if (args.tagUid.isNotEmpty()) {
            fetchAndShowEntityByTagUid(args.tagUid)
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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

    private fun fetchAndShowEntityByTagUid(tagUid: String) {
        val call = ApiClient.apiService.getEntitiesByTagUid(tagUid)

        call.enqueue(object : Callback<EnrichedEntityDTO> {
            override fun onResponse(
                call: Call<EnrichedEntityDTO>,
                response: Response<EnrichedEntityDTO>
            ) {
                if (!response.isSuccessful) {
                    val msg = String.format("Error: ${response.code()} ${response.message()}")
                    Log.e("API_ERROR", "Failure: ${msg}")
                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
                    return
                }

                val body = response.body()
                if (body == null) {
                    Toast.makeText(context, "Null", Toast.LENGTH_SHORT).show();
                    return
                }

                // If assign in progress, assign entity to tag
                (activity as MainActivity).assignParentOnEntityIdIsActive?.let {
                    assignParentDialog.dismiss()
                    assignEntityToTag(it, body.entity.id)
                    return
                }

                homeViewModel.entityClosure.value = body
            }

            override fun onFailure(call: Call<EnrichedEntityDTO>, t: Throwable) {
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

        call.enqueue(object : Callback<EnrichedEntityDTO> {
            override fun onResponse(call: Call<EnrichedEntityDTO>, response: Response<EnrichedEntityDTO>) {
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

            override fun onFailure(call: Call<EnrichedEntityDTO>, t: Throwable) {
                // Handle failure
                Log.e("API_ERROR", "Failure: ${t.message}")
                Toast.makeText(context, t.message, Toast.LENGTH_LONG).show()
                return
            }
        })
    }
}