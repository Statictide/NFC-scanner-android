package dk.sierrasoftware.nfcscanner.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import dk.sierrasoftware.nfcscanner.MainActivity
import dk.sierrasoftware.nfcscanner.api.EntityClient
import dk.sierrasoftware.nfcscanner.api.EntityClosureDTO
import dk.sierrasoftware.nfcscanner.api.MaybeInt
import dk.sierrasoftware.nfcscanner.api.PatchEntityDTO
import dk.sierrasoftware.nfcscanner.databinding.FragmentHomeBinding
import dk.sierrasoftware.nfcscanner.ui.dashboard.EntityAdapter
import kotlinx.coroutines.launch

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
                    view.clearFocus()
                    lifecycleScope.launch {
                        saveName(homeViewModel.entityClosure.value!!, name)
                    }
                }
                false
            }

            binding.deleteParentButton.setOnClickListener { view ->
                lifecycleScope.launch {
                    removeParent(homeViewModel.entityClosure.value!!)
                }
            }
        }

        binding.childrenRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = entityAdapter
        }

        // Fetch entity
        if (args.tagUid.isNotEmpty()) {
            lifecycleScope.launch {
                fetchAndShowEntityByTagUid(args.tagUid)
            }
        }

        if (args.entityId != 0) {
            lifecycleScope.launch {
                fetchAndShowEntity(args.entityId)
            }
        }

        setupAssignParentDialog(root)

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

    private fun showAssignParentPopup() {
        val entityId = homeViewModel.entityClosure.value!!.id
        (activity as MainActivity).assignParentOnEntityIdIsActive = entityId;
        assignParentDialog.show()
    }

    suspend fun fetchAndShowEntityByTagUid(tagUid: String) {
        EntityClient.client.getOrCreateEntityByTagUid(tagUid).onSuccess { entity ->
            // If assign in progress, assign entity to tag
            (activity as MainActivity).assignParentOnEntityIdIsActive?.let {
                assignParentDialog.dismiss()
                val entityBeingReassigned = it
                val newParentId = entity.id.toInt()
                assignEntityToTag(entityBeingReassigned, newParentId)
                return
            }

            homeViewModel.entityClosure.value = entity
        }.onFailure { t ->
            Toast.makeText(context, t.message, Toast.LENGTH_LONG).show()
        }
    }

    // Todo-
    suspend fun fetchAndShowEntity(entityId: Int) {
        EntityClient.client.getEntity(entityId).onSuccess { entity ->
            homeViewModel.entityClosure.value = entity
        }.onFailure { t ->
            Toast.makeText(context, t.message, Toast.LENGTH_LONG).show()
        }
    }


    private suspend fun saveName(entity: EntityClosureDTO, newName: String) {
        val patch = PatchEntityDTO(name = newName)
        EntityClient.client.patchEntity(entity.id, patch).onSuccess { newEntity ->
            Toast.makeText(context, "Saved", Toast.LENGTH_SHORT).show()
            homeViewModel.entityClosure.value = newEntity
        }.onFailure { t ->
            Toast.makeText(context, t.message, Toast.LENGTH_LONG).show()
        }
    }

    private suspend fun removeParent(entity: EntityClosureDTO) {
        val patch = PatchEntityDTO(parent_id = MaybeInt(null))
        EntityClient.client.patchEntity(entity.id, patch).onSuccess { newEntity ->
            Toast.makeText(context, "Saved", Toast.LENGTH_SHORT).show()
            homeViewModel.entityClosure.value = newEntity
        }.onFailure { t ->
            Toast.makeText(context, t.message, Toast.LENGTH_LONG).show()
        }
    }

    private suspend fun assignEntityToTag(entryId: Int, newParentId: Int) {
        val patchEntity = PatchEntityDTO(parent_id = MaybeInt(newParentId))
        EntityClient.client.patchEntity(entryId, patchEntity).onSuccess { entity ->
            homeViewModel.entityClosure.value = entity
        }.onFailure { t ->
            Toast.makeText(context, t.message, Toast.LENGTH_LONG).show()
        }

    }
}