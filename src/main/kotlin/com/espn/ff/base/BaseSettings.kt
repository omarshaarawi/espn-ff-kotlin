package com.espn.ff.base

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject

class BaseSettings(data: JsonElement) {

    private var playoffSeedTieRule: JsonElement?
    private var tieRule: JsonElement?
    private var name: JsonElement?
    var divisionMap: MutableMap<Any, Any>
    private var tradeDeadline: Double
    private var keeperCount: JsonElement?
    private var playoffTeamCount: JsonElement?
    private var teamCount: JsonElement?
    private var vetoVotesRequired: JsonElement?
    var matchupPeriods: JsonElement?
    private var regSeasonCount: JsonElement?

    init {
        this.regSeasonCount = data.jsonObject["scheduleSettings"]?.jsonObject?.get("matchupPeriodCount")
        this.matchupPeriods = data.jsonObject["scheduleSettings"]?.jsonObject?.get("matchupPeriods")
        this.vetoVotesRequired = data.jsonObject["tradeSettings"]?.jsonObject?.get("vetoVotesRequired")
        this.teamCount = data.jsonObject["size"]
        this.playoffTeamCount = data.jsonObject["scheduleSettings"]?.jsonObject?.get("playoffTeamCount")
        this.keeperCount = data.jsonObject["draftSettings"]?.jsonObject?.get("keeperCount")
        this.tradeDeadline = 0.0
        this.divisionMap = mutableMapOf()

        if (data.jsonObject["tradeSettings"]?.jsonObject?.contains("deadlineDate") == true) {
            this.tradeDeadline =
                data.jsonObject["tradeSettings"]?.jsonObject?.get("deadlineDate")?.toString()?.toDouble() ?: 0.0
        }
        this.name = data.jsonObject["name"]
        this.tieRule = data.jsonObject["scoringSettings"]?.jsonObject?.get("matchupTieRule")
        this.playoffSeedTieRule = data.jsonObject["scoringSettings"]?.jsonObject?.get("playoffMatchupTieRule")
        val divisions = data.jsonObject["scheduleSettings"]?.jsonObject?.get("divisions")
        for (division in divisions as JsonArray) {
            this.divisionMap[division.jsonObject["id"] as JsonElement] = division.jsonObject["name"] as JsonElement

        }

    }
}
