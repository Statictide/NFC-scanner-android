package dk.sierrasoftware.nfcscanner.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dk.sierrasoftware.nfcscanner.api.EnrichedEntityDTO

class HomeViewModel : ViewModel() {
    val entityClosure = MutableLiveData<EnrichedEntityDTO?>()
}
