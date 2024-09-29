package dk.sierrasoftware.nfcscanner.ui.notifications

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import dk.sierrasoftware.nfcscanner.databinding.FragmentNotificationsBinding
import dk.sierrasoftware.nfcscanner.ui.dashboard.OnEntitySelectedListener
import kotlinx.coroutines.launch

class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var notificationsViewModel: NotificationsViewModel;

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        notificationsViewModel = ViewModelProvider(this)[NotificationsViewModel::class.java]
        lifecycleScope.launch {
            notificationsViewModel.fetchAuditLog()
        }

        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)

        setupRecyclerView()
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupRecyclerView() {
        binding.logRecyclerview.layoutManager = LinearLayoutManager(context)

        // React to changes in model
        notificationsViewModel.auditLog.observe(viewLifecycleOwner) {
            binding.logRecyclerview.adapter = AuditLogAdapter(it)
        }
    }
}