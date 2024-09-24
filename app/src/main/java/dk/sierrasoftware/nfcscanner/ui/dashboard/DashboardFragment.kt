package dk.sierrasoftware.nfcscanner.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import dk.sierrasoftware.nfcscanner.MobileNavigationDirections
import dk.sierrasoftware.nfcscanner.databinding.FragmentDashboardBinding
import kotlinx.coroutines.launch

class DashboardFragment : Fragment() {

    companion object {
        val REQUEST_KEY: String = "DashboardFragment_REQUEST_KEY"
        val ENTITY_PICKER: String = "ENTITY_PICKER"
        val ENTITY_VIEWER: String = "ENTITY_VIEWER"
    }

    private var _binding: FragmentDashboardBinding? = null
    private val safeArgs: DashboardFragmentArgs by navArgs()

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    private lateinit var dashboardViewModel: DashboardViewModel;

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        dashboardViewModel = ViewModelProvider(this)[DashboardViewModel::class.java]
        lifecycleScope.launch {
            dashboardViewModel.fetchEntities()
        }

        _binding = FragmentDashboardBinding.inflate(inflater, container, false)

        val task: String = safeArgs.task
        if (task == ENTITY_PICKER) {
            setupRecyclerView(returnEntityToHomeFragment)
        } else {
            setupRecyclerView(navigateToEntity)
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupRecyclerView(listener: OnEntitySelectedListener? = null) {
        binding.entities.layoutManager = LinearLayoutManager(context)

        // React to changes in model
        dashboardViewModel.entities.observe(viewLifecycleOwner) {
            binding.entities.adapter = EntityAdapter(it.orEmpty(), listener = listener ?: navigateToEntity)
        }
    }

    private val returnEntityToHomeFragment = object : OnEntitySelectedListener {
        override fun onEntitySelected(entityId: Int) {
            setFragmentResult(REQUEST_KEY, bundleOf("entityId" to entityId))
            findNavController().navigateUp()
        }
    }

    private val navigateToEntity = object : OnEntitySelectedListener {
        override fun onEntitySelected(entityId: Int) {
            val action = DashboardFragmentDirections.actionNavigationHomeWithEntityId(entityId)
            findNavController().navigate(action)
        }
    }
}