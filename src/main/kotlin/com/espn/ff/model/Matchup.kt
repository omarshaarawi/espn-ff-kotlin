package com.espn.ff.model

import kotlin.properties.Delegates
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject

class Matchup(data: JsonElement) {

    var homeScore by Delegates.notNull<Double>()
    var awayScore by Delegates.notNull<Double>()
    lateinit var awayTeam: Any
    lateinit var homeTeam: Any
    private var data: JsonElement

    init {
        this.data = data
        this.fetchMatchupInfo()
    }

    fun fetchMatchupInfo() {
        this.homeTeam = this.data.jsonObject["home"]!!.jsonObject["teamId"].toString().toInt()
        this.homeScore = this.data.jsonObject["home"]!!.jsonObject["totalPoints"].toString().toDouble()

        this.awayTeam = 0
        this.awayScore = 0.0
        if ("away" in this.data.jsonObject) {
            this.awayTeam = this.data.jsonObject["away"]!!.jsonObject["teamId"].toString().toInt()
            this.awayScore = this.data.jsonObject["away"]!!.jsonObject["totalPoints"].toString().toDouble()
        }


    }

}
