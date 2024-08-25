package dk.sierrasoftware.nfcscanner.ui.home

import android.os.Parcelable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dk.sierrasoftware.nfcscanner.api.EntityClosureDTO
import kotlinx.parcelize.Parcelize
import java.util.Collections

class HomeViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is home Fragment"
    }

    val entityClosure = MutableLiveData<EntityClosureDTO?>()


    val text: LiveData<String> = _text
}
