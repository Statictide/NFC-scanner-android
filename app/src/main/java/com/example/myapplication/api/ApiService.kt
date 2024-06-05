package com.example.myapplication.api

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @GET("api/v1/entities/{id}")
    fun getEntity(@Path("id") id: Int): Call<Entity>

    @GET("api/v1/entities")
    fun getEntities(): Call<Array<Entity>>

    @GET("api/v1/entities")
    fun getEntitiesByTagId(@Query("tag_id") tagId: String): Call<Array<Entity>>
}

class Entity (val id: UInt, val tag_id: String, val name: String, val owner: String)
