package dk.sierrasoftware.nfcscanner.api

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @GET("api/v1/entities/by-tag")
    fun getEntitiesByTagUid(@Query("tag_uid") tagUid: String): Call<EntityClosureDTO>

    @PATCH("api/v1/entities/{id}")
    fun patchEntity(@Path("id") id: UInt, @Body entity: PatchEntityDTO): Call<EntityClosureDTO>


}

class EntityDTO (val id: UInt, val tag_uid: String, val name: String, val parent_id: UInt)
class EntityClosureDTO (val entity: EntityDTO, val parent: EntityDTO?, val children: Array<EntityDTO>)
class PatchEntityDTO ( val parent_id: UInt)