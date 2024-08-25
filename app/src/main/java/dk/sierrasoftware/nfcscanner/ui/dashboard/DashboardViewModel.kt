package dk.sierrasoftware.nfcscanner.ui.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dk.sierrasoftware.nfcscanner.api.EntityDTO

class DashboardViewModel : ViewModel() {
    val entities = MutableLiveData<List<EntityDTO>>().apply {
        value = listOf(EntityDTO(2u, "049F3972FE4A80", "Main entity 1", 1u))
    }
}