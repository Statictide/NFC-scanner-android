package dk.sierrasoftware.nfcscanner.api

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @GET("/api/v0/entities/by-tag")
    fun getEntitiesByTagUid(@Query("tag_uid") tagUid: String): Call<EntityClosureDTO>

    @GET("/api/v0/entities")
    fun getEntitiesByUser(@Query("user_id") userId: UInt): Call<List<EntityClosureDTO>>


    @PATCH("/api/v0/entities/{id}")
    fun patchEntity(@Path("id") id: UInt, @Body entity: PatchEntityDTO): Call<EntityClosureDTO>
}


