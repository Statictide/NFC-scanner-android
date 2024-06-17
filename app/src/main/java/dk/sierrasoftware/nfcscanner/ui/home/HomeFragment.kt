package dk.sierrasoftware.nfcscanner.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import dk.sierrasoftware.nfcscanner.R
import dk.sierrasoftware.nfcscanner.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!


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

        //val textView: TextView = binding.textHome
        homeViewModel.text.observe(viewLifecycleOwner) {
            //textView.text = it
        }

        // Bind views
        assignButton = root.findViewById(R.id.assign_button)

        arguments?.getString("tag_uid")?.let { binding.tagIdValue.text = it } ?: run {
            binding.tagIdValue.text = R.string.na.toString()
        }

        arguments?.getString("name")?.let { binding.nameValue.text = it }?: run {
            binding.nameValue.text = R.string.na.toString()
        }

        arguments?.getString("tag_uid")?.let { assignButton.isEnabled = true }?: run {
            assignButton.isEnabled = false
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}