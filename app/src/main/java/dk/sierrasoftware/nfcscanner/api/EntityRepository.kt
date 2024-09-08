package dk.sierrasoftware.nfcscanner.api

import android.util.Log

object EntityClient {
    val client: EntityRepository by lazy {
        EntityRepository(ApiClient.apiService)
    }
}

class EntityRepository(private val apiService: ApiService) {
    suspend fun getEntitiesByUser(userId: Int): Result<List<EntityClosureDTO>> {
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

    suspend fun getOrCreateEntityByTagUid(tag_uid: String): Result<EntityClosureDTO> {
        return try {
            val response = apiService.getEntityByTagUid(tag_uid, true)
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

    suspend fun getEntity(entityId: Int): Result<EntityClosureDTO> {
        return try {
            val response = apiService.getEntity(entityId)
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

    suspend fun patchEntity(id: Int, entity: PatchEntityDTO): Result<EntityClosureDTO> {
        return try {
            val response = apiService.patchEntity(id, entity)
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

    suspend fun createEntity(entity: CreateEntityDTO): Result<EntityClosureDTO> {
        return try {
            val response = apiService.createEntity(entity)
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Log.e("API_ERROR", "Failure: ${response.message()}");
                Result.failure(Exception("API error: ${response.code()} ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e("API_ERROR", "Failure: ${e.message}, ${e.cause}");
            Result.failure(e)
        }
    }

    suspend fun deleteEntity(entityId: Int): Result<Unit> {
        return try {
            val response = apiService.deleteEntity(entityId)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Log.e("API_ERROR", "Failure: ${response.message()}");
                Result.failure(Exception("API error: ${response.code()} ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e("API_ERROR", "Failure: ${e.message}, ${e.cause}");
            Result.failure(e)
        }
    }
}