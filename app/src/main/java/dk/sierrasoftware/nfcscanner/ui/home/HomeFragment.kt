package dk.sierrasoftware.nfcscanner.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import dk.sierrasoftware.nfcscanner.api.EntityClient
import dk.sierrasoftware.nfcscanner.api.EntityClosureDTO
import dk.sierrasoftware.nfcscanner.api.MaybeInt
import dk.sierrasoftware.nfcscanner.api.MaybeString
import dk.sierrasoftware.nfcscanner.api.PatchEntityDTO
import dk.sierrasoftware.nfcscanner.databinding.FragmentHomeBinding
import dk.sierrasoftware.nfcscanner.ui.dashboard.DashboardFragment
import dk.sierrasoftware.nfcscanner.ui.dashboard.EntityAdapter
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    private val safeArgs: HomeFragmentArgs by navArgs()

    private lateinit var homeViewModel: HomeViewModel;

    private lateinit var assignParentDialog: AlertDialog
    private lateinit var assignTagDialog: AlertDialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        assignParentDialog = setupAssignParentDialog(root)
        assignTagDialog = setupAssignTagDialog(root)

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Bind view model
        binding.fragmentHomeEntityGroup.visibility = View.GONE
        binding.fragmentHomeChildrenRecyclerView.layoutManager = LinearLayoutManager(context)
        homeViewModel.entityClosure.observe(viewLifecycleOwner) {
            renderEntity(it)
        }

        // Fetch entity
        if (safeArgs.tagUid.isNotEmpty()) {
            lifecycleScope.launch {
                fetchAndShowEntityByTagUid(safeArgs.tagUid)
            }
        }

        if (safeArgs.entityId != 0) {
            lifecycleScope.launch {
                fetchAndShowEntity(safeArgs.entityId)
            }
        }
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
        binding.parentView.text = entity.parent_name
        if (entity.parent_id != null) {
            binding.parentView.setOnClickListener {
                lifecycleScope.launch { fetchAndShowEntity(entity.parent_id) }
            }
        }
        binding.tagTextView.text = entity.tag_uid ?: "None"
        binding.fragmentHomeChildrenRecyclerView.adapter = EntityAdapter(listOf(entity), showName = false)

        // Delete entity
        binding.fragmentHomeDeleteButton.setOnClickListener {
            lifecycleScope.launch { deleteEntity(entity.id) }
        }

        // Assign parent
        binding.assignButton.isEnabled = true
        binding.selectParentButton.isEnabled = true


        // Remove parent
        binding.deleteParentButton.setOnClickListener {
            lifecycleScope.launch {
                removeParent(homeViewModel.entityClosure.value!!)
            }
        }

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

        // Assign tag
        binding.asignTagButton.setOnClickListener {
            assignTagDialog.show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        assignParentDialog.dismiss()
        _binding = null
    }

    private fun setupAssignParentDialog(view: View): AlertDialog {
        val builder = AlertDialog.Builder(view.context)
        builder.setTitle("NFC Scan")
        builder.setMessage("Approach an NFC tag to assign.")

        assignParentDialog = builder.create();

        binding.assignButton.setOnClickListener {
            assignParentDialog.show()
        }

        binding.selectParentButton.setOnClickListener {
            setFragmentResultListener(DashboardFragment.REQUEST_KEY) { _, bundle ->
                val newParentId = bundle.getInt("entityId")
                lifecycleScope.launch {
                    assignEntityParent(homeViewModel.entityClosure.value!!.id, newParentId)
                }
            }

            val action = HomeFragmentDirections.actionNavigationHomeToNavigationDashboard(DashboardFragment.ENTITY_PICKER)
            findNavController().navigate(action)
        }

        return assignParentDialog
    }

    private fun setupAssignTagDialog(view: View): AlertDialog {
        val builder = AlertDialog.Builder(view.context)
        builder.setTitle("NFC Scan")
        builder.setMessage("Approach an NFC tag to assign.")

        val dialog = builder.create();

        binding.asignTagButton.setOnClickListener {
            dialog.show()
        }

        return dialog
    }

    fun onNfcEventReceived(tagUid: String) {
        lifecycleScope.launch {
            fetchAndShowEntityByTagUid(tagUid)
        }
    }

    private suspend fun fetchAndShowEntityByTagUid(tagUid: String) {
        if (assignTagDialog.isShowing) {
            assignTagDialog.dismiss()
            val entityBeingReassigned = homeViewModel.entityClosure.value!!.id
            val newTagUid = tagUid
            assignEntityTag(entityBeingReassigned, newTagUid)
            return
        }

        EntityClient.client.getOrCreateEntityByTagUid(tagUid).onSuccess { entity ->
            // If assign in progress, assign entity to tag
            if (assignParentDialog.isShowing) {
                assignParentDialog.dismiss()
                val entityBeingReassigned = homeViewModel.entityClosure.value!!.id
                val newParentId = entity.id
                assignEntityParent(entityBeingReassigned, newParentId)
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

    private suspend fun assignEntityParent(entryId: Int, newParentId: Int) {
        val patchEntity = PatchEntityDTO(parent_id = MaybeInt(newParentId))
        EntityClient.client.patchEntity(entryId, patchEntity).onSuccess { entity ->
            // Navigate to dashboard
            homeViewModel.entityClosure.value = entity
            val action = HomeFragmentDirections.actionNavigationHomeToNavigationDashboard(DashboardFragment.ENTITY_VIEWER)
            findNavController().navigate(action)
        }.onFailure { t ->
            Toast.makeText(context, t.message, Toast.LENGTH_LONG).show()
        }
    }

    private suspend fun assignEntityTag(entryId: Int, newTagUid: String) {
        val patchEntity = PatchEntityDTO(tag_uid = MaybeString(newTagUid))
        EntityClient.client.patchEntity(entryId, patchEntity).onSuccess { entity ->
            homeViewModel.entityClosure.value = entity
        }.onFailure { t ->
            Toast.makeText(context, t.message, Toast.LENGTH_LONG).show()
        }
    }

    private suspend fun deleteEntity(entryId: Int) {
        EntityClient.client.deleteEntity(entryId).onSuccess {
            homeViewModel.entityClosure.value = null
        }.onFailure { t ->
            Toast.makeText(context, t.message, Toast.LENGTH_LONG).show()
        }
    }
}