package dk.sierrasoftware.nfcscanner.api

import android.util.Log


object EntityClient {
    val client: EntityRepository by lazy {
        EntityRepository(ApiClient.apiService)
    }
}

class EntityRepository(private val apiService: ApiService) {
    suspend fun getEntitiesByUser(userId: UInt): Result<List<EntityClosureDTO>> {
        return try {
            val response = apiService.getEntitiesByUser(userId)
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

    suspend fun getOrCreateEntitiesByTagUid(tag_uid: String): Result<EntityClosureDTO> {
        return try {
            val response = apiService.getEntitiesByTagUid(tag_uid, true)
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