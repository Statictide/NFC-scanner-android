package dk.sierrasoftware.nfcscanner.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import dk.sierrasoftware.nfcscanner.R
import dk.sierrasoftware.nfcscanner.api.EntityClosureDTO
import dk.sierrasoftware.nfcscanner.api.EntityDTO
import dk.sierrasoftware.nfcscanner.databinding.FragmentHomeBinding
import java.util.Collections

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val args: HomeFragmentArgs by navArgs()


    // Views
    private lateinit var assignButton: Button

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        val homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Bind view model
        homeViewModel.entityClosure.observe(viewLifecycleOwner) {
            renderEntity(it)
        }

        homeViewModel.entityClosure.value = args.entityClosure

        return root
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun assignEntityTo(view: View) {}
}