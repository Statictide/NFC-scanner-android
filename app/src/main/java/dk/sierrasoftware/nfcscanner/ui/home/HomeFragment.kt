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

        arguments?.getString("tag_uid")?.let { binding.tagIdValue.text = it }
        arguments?.getString("name")?.let { binding.nameValue.text = it }
        arguments?.getString("tag_uid")?.let { assignButton.isEnabled = true }
        arguments?.getString("parent")?.let { binding.parentValue.text = it }


        // Example data
        val items = listOf("Item 1", "Item 2", "Item 3", "Item 4")

        // Find the LinearLayout in the ScrollView

        // Iterate over the list and create TextView for each item
        pupulateChildrenLayout(items)

        return root
    }

    private fun pupulateChildrenLayout(items: List<String>) {
        val linearLayout: LinearLayout = binding.childrenLayout

        for (item in items) {
            val textView = TextView(context)
            textView.text = item
            textView.textSize = 18f  // Customize the text size if needed
            textView.setPadding(0, 10, 0, 10)  // Add padding if needed

            // Add the TextView to the LinearLayout
            linearLayout.addView(textView)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}