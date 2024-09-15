package dk.sierrasoftware.nfcscanner.ui.dashboard

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dk.sierrasoftware.nfcscanner.api.EntityClient
import dk.sierrasoftware.nfcscanner.api.EntityClosureDTO

class DashboardViewModel : ViewModel() {

    val _entities = MutableLiveData<List<EntityClosureDTO>>()
    val entities: LiveData<List<EntityClosureDTO>> = _entities;


    suspend fun fetchEntities() {
        val result = EntityClient.client.getEntitiesByUser(1)
        result.onSuccess { entities ->
            Log.i("API RESPONSE", "Got response")
            this._entities.value = entities
        }.onFailure { _ ->
        }
    }
}
