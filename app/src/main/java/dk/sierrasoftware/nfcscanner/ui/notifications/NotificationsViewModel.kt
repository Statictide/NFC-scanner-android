package dk.sierrasoftware.nfcscanner.ui.notifications

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dk.sierrasoftware.nfcscanner.api.AuditLogClient
import dk.sierrasoftware.nfcscanner.api.AuditLogEntry
import java.time.LocalDateTime

class NotificationsViewModel : ViewModel() {

    private val _auditLog = MutableLiveData<List<AuditLogEntry>>().apply {
        value = listOf(
            AuditLogEntry(
                1,
                "Glock",
                "BankBox",
                "Lasse",
                "2024-10-02T14:24:53"
            )
        )
    }
    val auditLog: LiveData<List<AuditLogEntry>> = _auditLog

    suspend fun fetchAuditLog() {
        val result = AuditLogClient.client.getAuditLog()
        result.onSuccess { response ->
            Log.i("API RESPONSE", "Got response")
            this._auditLog.value = response.parent_history
        }.onFailure { t ->
            Log.e("API RESPONSE", "Failed to get audit log" + t.message, t)
        }
    }
}