package com.espn.ff.service

import kotlinx.serialization.json.JsonElement
import retrofit2.Call
import retrofit2.http.*

interface EspnService {

    @GET
    fun get(
        @Url endpoint: String,
        @Query("view") view: List<String>,
        @QueryMap params: Map<String, String>,
        @HeaderMap headers: Map<String, String>,
    ): Call<JsonElement>
}
