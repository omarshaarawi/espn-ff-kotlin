package com.espn.ff.service

import com.espn.ff.util.EspnConstants
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import okhttp3.MediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit


object EspnClient {

    private val contentType = MediaType.get("application/json")

    private val json = Json {
        ignoreUnknownKeys = true; allowStructuredMapKeys = true; prettyPrint = true
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun createEspnService(cookies: String): EspnService {
        val retrofit = Retrofit.Builder().addConverterFactory((json.asConverterFactory(contentType)))
            .baseUrl(EspnConstants.FANTASY_BASE_ENDPOINT).client(OkHttpClient.Builder().addInterceptor { chain ->
                chain.request().newBuilder().addHeader("Cookie", cookies).build().let { chain.proceed(it) }
            }.build()).build()
        return retrofit.create(EspnService::class.java)
    }


    fun checkRequestStatus(status: Int) {

        when {
            status in 500..503 step 1 -> throw Exception()
            status == 403 -> throw Exception()
            status == 404 -> throw Exception()
            status != 200 -> throw Exception()
        }
        if (status in 500..503 step 1) {
            throw Exception()
        }
    }
}