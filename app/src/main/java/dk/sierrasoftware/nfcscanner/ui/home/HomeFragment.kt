package dk.sierrasoftware.nfcscanner.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import dk.sierrasoftware.nfcscanner.MainActivity
import dk.sierrasoftware.nfcscanner.api.ApiClient
import dk.sierrasoftware.nfcscanner.api.EntityClosureDTO
import dk.sierrasoftware.nfcscanner.api.MaybeInt
import dk.sierrasoftware.nfcscanner.api.PatchEntityDTO
import dk.sierrasoftware.nfcscanner.databinding.FragmentHomeBinding
import dk.sierrasoftware.nfcscanner.ui.dashboard.EntityAdapter
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
                binding.entityGroup.isEnabled = false
                return@observe
            }

            binding.entityGroup.isEnabled = true

            // Handle it
            binding.nameView.setText(it.name)
            binding.parentView.text = it.parent_name
            binding.assignButton.isEnabled = true
            entityAdapter = EntityAdapter(it.children)
            binding.childrenRecyclerView.adapter = EntityAdapter(it.children )

            // Save name on update
            binding.nameView.setOnEditorActionListener { view, actionId, event ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    val name = view.getText().toString()
                    saveName(homeViewModel.entityClosure.value!!, name)
                    view.clearFocus()
                }
                false
            }

            binding.deleteParentButton.setOnClickListener { view ->
                removeParent(homeViewModel.entityClosure.value!!)
            }
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
        val call = ApiClient.apiService.getEntitiesByTagUid(tagUid, true)

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

                val body = response.body()!!

                // If assign in progress, assign entity to tag
                (activity as MainActivity).assignParentOnEntityIdIsActive?.let {
                    assignParentDialog.dismiss()
                    assignEntityToTag(it, body.id.toInt())
                    return
                }

                homeViewModel.entityClosure.value = body
            }

            override fun onFailure(call: Call<EntityClosureDTO>, t: Throwable) {
                // TODO: Add option to create new entity
                Log.e("API_ERROR", "Failure: ${t.message}")
                Toast.makeText(context, t.message, Toast.LENGTH_LONG).show()
                return
            }
        })
    }


    private fun saveName(entity: EntityClosureDTO, newName: String) {
        val patch = PatchEntityDTO(name = newName)
        val call = ApiClient.apiService.patchEntity(entity.id, patch)

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

                val body = response.body()!!

                Toast.makeText(context, "Saved", Toast.LENGTH_SHORT).show()
                homeViewModel.entityClosure.value = body
            }

            override fun onFailure(call: Call<EntityClosureDTO>, t: Throwable) {
                Log.e("API_ERROR", "Failure: ${t.message}")
                Toast.makeText(context, t.message, Toast.LENGTH_LONG).show()
                return
            }
        })
    }

    private fun removeParent(entity: EntityClosureDTO) {
        val patch = PatchEntityDTO(parent_id = MaybeInt(null))
        val call = ApiClient.apiService.patchEntity(entity.id, patch)

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

                val body = response.body()!!

                Toast.makeText(context, "Saved", Toast.LENGTH_SHORT).show()
                homeViewModel.entityClosure.value = body
            }

            override fun onFailure(call: Call<EntityClosureDTO>, t: Throwable) {
                Log.e("API_ERROR", "Failure: ${t.message}")
                Toast.makeText(context, t.message, Toast.LENGTH_LONG).show()
                return
            }
        })
    }

    private fun showAssignParentPopup() {
        val entityId = homeViewModel.entityClosure.value!!.id
        (activity as MainActivity).assignParentOnEntityIdIsActive = entityId;
        assignParentDialog.show()
    }

    private fun assignEntityToTag(entryId: UInt, newParentId: Int) {
        val patchEntity = PatchEntityDTO(parent_id = MaybeInt(newParentId))
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

                homeViewModel.entityClosure.value = response.body()!!
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