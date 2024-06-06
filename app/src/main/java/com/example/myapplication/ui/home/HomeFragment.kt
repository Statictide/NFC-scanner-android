package com.example.myapplication.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!


    // Views
    private lateinit var tagIdTextView: TextView
    private lateinit var nameTextView: TextView
    private lateinit var ownerTextView: TextView
    private lateinit var tagImageView: ImageView

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

        // Extracting views
        tagIdTextView = root.findViewById(R.id.tag_id_value)
        nameTextView = root.findViewById(R.id.name_value)
        ownerTextView = root.findViewById(R.id.owner_value)
        tagImageView = root.findViewById(R.id.tag_image)

        arguments?.getString("tag_id")?.let { tagIdTextView.text = it }
        arguments?.getString("name")?.let { nameTextView.text = it }
        arguments?.getString("owner")?.let { ownerTextView.text = it }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}