package dk.sierrasoftware.nfcscanner.api

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("api/v1/entities/by-tag")
    fun getEntitiesByTagUid(@Query("tag_uid") tagUid: String): Call<EntityClosureDTO>


}

class EntityDTO (val id: UInt, val tag_uid: String, val name: String, val parrent_id: UInt)
class EntityClosureDTO (val entity: EntityDTO, val parrent: EntityDTO?, val children: Array<EntityDTO>)
