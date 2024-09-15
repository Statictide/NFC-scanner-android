package dk.sierrasoftware.nfcscanner.ui.dashboard

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import dk.sierrasoftware.nfcscanner.api.EntityClient
import dk.sierrasoftware.nfcscanner.databinding.FragmentDashboardBinding
import kotlinx.coroutines.launch

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null

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

        setupRecyclerView()

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupRecyclerView() {
        binding.entities.layoutManager = LinearLayoutManager(context)
        binding.entities.adapter = EntityAdapter(dashboardViewModel.entities.value.orEmpty())

        // React to changes in model
        dashboardViewModel.entities.observe(viewLifecycleOwner) {
            binding.entities.adapter = EntityAdapter(it.orEmpty())
        }
    }
}