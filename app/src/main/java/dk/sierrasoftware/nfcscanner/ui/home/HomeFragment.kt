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
import dk.sierrasoftware.nfcscanner.api.EntityClient
import dk.sierrasoftware.nfcscanner.api.EntityClosureDTO
import dk.sierrasoftware.nfcscanner.api.MaybeInt
import dk.sierrasoftware.nfcscanner.api.PatchEntityDTO
import dk.sierrasoftware.nfcscanner.databinding.FragmentHomeBinding
import dk.sierrasoftware.nfcscanner.ui.dashboard.EntityAdapter
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    private val args: HomeFragmentArgs by navArgs()

    private lateinit var homeViewModel: HomeViewModel;

    private var assignParentOnEntityIdIsActive: Int? =
        null // Signal to assign entity to tag on next NFC scan
    private lateinit var assignParentDialog: AlertDialog

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.fragmentHomeChildrenRecyclerView.layoutManager = LinearLayoutManager(context)

        // Bind view model
        homeViewModel.entityClosure.observe(viewLifecycleOwner) {
            renderEntity(it)
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

    private fun renderEntity(entity: EntityClosureDTO?) {
        // Handle null
        if (entity == null) {
            binding.fragmentHomeEntityGroup.visibility = View.GONE
            return
        }

        binding.fragmentHomeEntityGroup.visibility = View.VISIBLE

        // Set values
        binding.nameView.setText(entity.name)
        if (entity.parent_id != null) {
            binding.parentView.text = entity.parent_name
            binding.parentView.setOnClickListener {
                lifecycleScope.launch {
                    fetchAndShowEntity(entity.parent_id)
                }
            }
        }
        binding.assignButton.isEnabled = true
        binding.fragmentHomeChildrenRecyclerView.adapter = EntityAdapter(listOf(entity), false)

        // Save name on update
        binding.nameView.setOnEditorActionListener { view, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val name = view.text.toString()
                view.clearFocus()
                lifecycleScope.launch {
                    saveName(homeViewModel.entityClosure.value!!, name)
                }
            }
            false
        }

        binding.deleteParentButton.setOnClickListener {
            lifecycleScope.launch {
                removeParent(homeViewModel.entityClosure.value!!)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        assignParentDialog.dismiss()
        _binding = null
    }

    private fun setupAssignParentDialog(view: View) {
        val builder = AlertDialog.Builder(view.context)
        builder.setTitle("NFC Scan")
        builder.setMessage("Approach an NFC tag to assign.")

        // Set id to signal assignment on next NFC scan
        // Remove it when dialog is dismissed, to remove assignment signal
        builder.setOnDismissListener {
            assignParentOnEntityIdIsActive = null;
        }

        assignParentDialog = builder.create();

        binding.assignButton.setOnClickListener {
            showAssignParentPopup()
        }
    }

    private fun showAssignParentPopup() {
        val entityId = homeViewModel.entityClosure.value!!.id
        assignParentOnEntityIdIsActive = entityId;
        assignParentDialog.show()
    }

    fun onNfcEventReceived(tagUid: String) {
        lifecycleScope.launch {
            fetchAndShowEntityByTagUid(tagUid)
        }
    }

    private suspend fun fetchAndShowEntityByTagUid(tagUid: String) {
        EntityClient.client.getOrCreateEntityByTagUid(tagUid).onSuccess { entity ->
            // If assign in progress, assign entity to tag
            assignParentOnEntityIdIsActive?.let {
                assignParentDialog.dismiss()
                val entityBeingReassigned = it
                val newParentId = entity.id
                assignEntityToTag(entityBeingReassigned, newParentId)
                return
            }

            homeViewModel.entityClosure.value = entity
        }.onFailure { t ->
            Toast.makeText(context, t.message, Toast.LENGTH_LONG).show()
        }
    }

    private suspend fun fetchAndShowEntity(entityId: Int) {
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