package dk.sierrasoftware.nfcscanner.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @GET("/api/v0/entities")
    suspend fun getEntitiesByUser(@Query("user_id") userId: Int): Response<List<EntityClosureDTO>>
    @POST("/api/v0/entities")
    suspend fun createEntity(@Body entity: CreateEntityDTO): Response<EntityClosureDTO>

    @GET("/api/v0/entities/{id}")
    suspend fun getEntity(@Path("id") id: Int): Response<EntityClosureDTO>
    //@PUT("/api/v0/entities/{id}")
    //suspend fun updateEntity(@Path("id") id: Int, @Body entity: CreateEntityDTO): Response<EntityClosureDTO>
    @PATCH("/api/v0/entities/{id}")
    suspend fun patchEntity(@Path("id") id: Int, @Body entity: PatchEntityDTO): Response<EntityClosureDTO>
    @DELETE("/api/v0/entities/{id}")
    suspend fun deleteEntity(@Path("id") id: Int): Response<Void>

    @GET("/api/v0/entities/by-tag")
    suspend fun getEntityByTagUid(@Query("tag_uid") tagUid: String, @Query("create")  create: Boolean = false): Response<EntityClosureDTO>

    @POST("/api/v0/check-for-update")
    suspend fun checkForUpdate(@Body entity: CheckForUpdateDTO): Response<CheckForUpdateResponseDTO>

    @GET("/api/v0/history")
    suspend fun getHistory (): Response<AuditLogResponse>

}


