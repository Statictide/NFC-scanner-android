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


data class EntityDTO(
    val id: UInt?, val tagUid: String?, val name: String?, val parentId: UInt?
)

data class EntityClosureDTO(
    val entity: EntityDTO,
    val parent: EntityDTO? = null,
    val children: List<EntityDTO> = emptyList()
)
