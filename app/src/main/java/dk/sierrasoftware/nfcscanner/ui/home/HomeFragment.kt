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

    private lateinit var assignParentInProcessDialog: AlertDialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
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
        binding.parentView.text = entity.parent_name
        if (entity.parent_id != null) {
            binding.parentView.setOnClickListener {
                lifecycleScope.launch { fetchAndShowEntity(entity.parent_id) }
            }
        }

        // Delete entity
        binding.fragmentHomeDeleteButton.setOnClickListener {
            lifecycleScope.launch { deleteEntity(entity.id) }
        }


        binding.assignButton.isEnabled = true
        binding.selectParentButton.isEnabled = true
        binding.fragmentHomeChildrenRecyclerView.adapter =
            EntityAdapter(listOf(entity), showName = false)

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
        assignParentInProcessDialog.dismiss()
        _binding = null
    }

    private fun setupAssignParentDialog(view: View) {
        val builder = AlertDialog.Builder(view.context)
        builder.setTitle("NFC Scan")
        builder.setMessage("Approach an NFC tag to assign.")

        assignParentInProcessDialog = builder.create();

        binding.assignButton.setOnClickListener {
            assignParentInProcessDialog.show()
        }

        binding.selectParentButton.setOnClickListener {
            setFragmentResultListener(DashboardFragment.REQUEST_KEY) { _, bundle ->
                val newParentId = bundle.getInt("entityId")
                lifecycleScope.launch {
                    assignEntityToTag(homeViewModel.entityClosure.value!!.id, newParentId)
                }
            }

            val action = HomeFragmentDirections.actionNavigationHomeToNavigationDashboard(DashboardFragment.ENTITY_PICKER)
            findNavController().navigate(action)
        }
    }

    fun onNfcEventReceived(tagUid: String) {
        lifecycleScope.launch {
            fetchAndShowEntityByTagUid(tagUid)
        }
    }

    private suspend fun fetchAndShowEntityByTagUid(tagUid: String) {
        EntityClient.client.getOrCreateEntityByTagUid(tagUid).onSuccess { entity ->
            // If assign in progress, assign entity to tag
            if (assignParentInProcessDialog.isShowing) {
                assignParentInProcessDialog.dismiss()
                val entityBeingReassigned = homeViewModel.entityClosure.value!!.id
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

    private suspend fun deleteEntity(entryId: Int) {
        EntityClient.client.deleteEntity(entryId).onSuccess {
            homeViewModel.entityClosure.value = null
        }.onFailure { t ->
            Toast.makeText(context, t.message, Toast.LENGTH_LONG).show()
        }
    }
}