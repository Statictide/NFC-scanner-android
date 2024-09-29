package dk.sierrasoftware.nfcscanner.api

import android.util.Log

object AuditLogClient {
    val client: AuditLogRepository by lazy {
        AuditLogRepository(ApiClient.apiService)
    }
}

class AuditLogRepository(private val apiService: ApiService) {
    suspend fun getAuditLog(): Result<AuditLogResponse> {
        return try {
            val response = apiService.getHistory()
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Log.e("API_ERROR", "Failure: ${response.message()}");
                Result.failure(Exception("API error: ${response.code()} ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}