package dk.sierrasoftware.nfcscanner.api;

import android.util.Log

object UtilClient {
    val client: UtilRepository by lazy {
        UtilRepository(ApiClient.apiService)
    }
}

class UtilRepository(private val apiService: ApiService) {
    suspend fun checkForUpdate(version: CheckForUpdateDTO): Result<CheckForUpdateResponseDTO> {
        return try {
            val response = apiService.checkForUpdate(version)
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