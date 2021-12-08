package com.espn.ff.service

import com.google.gson.Gson
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import mu.KLogger

class EspnRequests(year: Int, leagueId: Int, swid: String, espnS2: String, logger: KLogger) {
    private val cookies = "SWID=$swid; espn_s2=$espnS2"
    private val espnEndpoints by lazy { EspnClient.createEspnService(cookies) }

    var leagueEndpoint = "ffl"
    var endpoint: String = "ffl/seasons/${year}"
    private val gson = Gson()

    init {
        leagueEndpoint = if (year < 2018) {
            "${this.leagueEndpoint}leagueHistory/$leagueId?seasonId=$year"
        } else {
            "${this.leagueEndpoint}/seasons/$year/segments/0/leagues/$leagueId"
        }
    }

    fun leagueGet(
        views: List<String> = emptyList(),
        params: Map<String, String> = emptyMap(),
        headers: Map<String, String> = emptyMap(),
        extend: String = "",
    ): JsonObject {
        val endpoint = this.leagueEndpoint + extend
        val r = espnEndpoints.get(endpoint, views, params, headers).execute()
        EspnClient.checkRequestStatus(r.code())

        return r.body()!!.jsonObject

    }

    private fun get(
        views: List<String> = emptyList(),
        params: Map<String, String> = emptyMap(),
        headers: Map<String, String> = emptyMap(),
        extend: String = "",
    ): JsonElement {
        val endpoint = this.endpoint + extend
        val r = espnEndpoints.get(endpoint, views, params, headers).execute()
        EspnClient.checkRequestStatus(r.code())
        return r.body()!!
    }

    fun getLeague(): JsonObject? {
        val views = listOf(
            "mTeam", "mSchedule", "mRoster", "mMatchup", "mSettings"
        )
        return leagueGet(views)
    }

    fun getProSchedules(): JsonElement {
        val views = listOf("proTeamSchedules_wl")
        return get(views)
    }

    fun getProPlayers(): JsonElement {
        val views = listOf("players_wl")
        val filters = mapOf(
            "filterActive" to mapOf("value" to true)
        )
        val headers = mapOf("x-fantasy-filter" to gson.toJson(filters))

        return get(views = views, headers = headers, extend = "/players")
    }

    fun getLeagueDraft(): JsonObject? {
        val views = listOf("mDraftDetail")
        return leagueGet(views)
    }
}