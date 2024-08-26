package dk.sierrasoftware.nfcscanner.ui.dashboard

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import dk.sierrasoftware.nfcscanner.api.ApiClient
import dk.sierrasoftware.nfcscanner.api.EntityClosureDTO
import dk.sierrasoftware.nfcscanner.databinding.FragmentDashboardBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var dashboardViewModel: DashboardViewModel;

    private lateinit var entitiesAdapter: EntitiesAdapter

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        dashboardViewModel = ViewModelProvider(this)[DashboardViewModel::class.java]
        fetchAndShowEntities();

        _binding = FragmentDashboardBinding.inflate(inflater, container, false)


        setupRecyclerView()

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupRecyclerView() {
        entitiesAdapter = EntitiesAdapter(dashboardViewModel.entities.value.orEmpty()) // Initially empty list

        dashboardViewModel.entities.observe(viewLifecycleOwner) {
            entitiesAdapter = EntitiesAdapter(it.orEmpty())
            binding.recyclerViewEntities.adapter = entitiesAdapter
        }

        binding.recyclerViewEntities.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = entitiesAdapter
        }
    }


    private fun fetchAndShowEntities() {
        val call = ApiClient.apiService.getEntitiesByUser(1u)

        call.enqueue(object : Callback<List<EntityClosureDTO>> {
            override fun onResponse(
                call: Call<List<EntityClosureDTO>>,
                response: Response<List<EntityClosureDTO>>
            ) {
                if (!response.isSuccessful) {
                    val msg = String.format("Error: ${response.code()} ${response.message()}")
                    Log.e("API_ERROR", "Failure: ${msg}")
                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
                    return
                }

                val body = response.body()
                if (body == null) {
                    Toast.makeText(context, "Null", Toast.LENGTH_SHORT).show();
                    return
                }

                dashboardViewModel.entities.value = body
            }

            override fun onFailure(call: Call<List<EntityClosureDTO>>, t: Throwable) {
                // TODO: Add option to create new entity
                Log.e("API_ERROR", "Failure: ${t.message}")
                Toast.makeText(context, t.message, Toast.LENGTH_LONG).show()
                return
            }
        })
    }
}