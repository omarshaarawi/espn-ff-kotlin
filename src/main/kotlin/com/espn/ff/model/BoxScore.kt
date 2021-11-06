package com.espn.ff.model

import com.espn.ff.util.EspnUtils.round
import kotlin.properties.Delegates
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject

class BoxScore(
    data: JsonElement,
    proSchedule: MutableMap<Any, Any>,
    positionalRankings: MutableMap<Int, Any>,
    week: Int,
    year: Int,
) {
    var awayProjected: Double
    var awayLineup = mutableListOf<BoxPlayer>()
    var awayScore: Double
    var awayTeam: Any
    private var awayRoster: JsonArray
    var homeLinup = mutableListOf<BoxPlayer>()
    private var homeRoster: JsonArray
    var homeProjected by Delegates.notNull<Double>()
    var homeScore: Double
    var homeTeam: Any

    init {
        this.homeTeam = data.jsonObject["home"]!!.jsonObject["teamId"].toString().toInt()
        this.homeProjected = (-1).toDouble()

        if ("totalPointsLive" in data.jsonObject["home"].toString()) {
            this.homeScore = data.jsonObject["home"]!!.jsonObject["totalPointsLive"].toString().toDouble().round(2)
            this.homeProjected =
                data.jsonObject["home"]!!.jsonObject.getOrDefault("totalProjectedPointsLive", -1).toString().toDouble()
                    .round(2)
        } else if ("totalPoints" in data.jsonObject["home"].toString()) {
            this.homeScore =
                data.jsonObject["home"]!!.jsonObject["totalPoints"].toString()
                    .toDouble()
        } else {
            this.homeScore =
                data.jsonObject["home"]!!.jsonObject["rosterForCurrentScoringPeriod"]!!.jsonObject["entries"].toString()
                    .toDouble()
        }

        this.homeRoster =
            data.jsonObject["home"]!!.jsonObject["rosterForCurrentScoringPeriod"]!!.jsonObject["entries"]!!.jsonArray

        for (player in homeRoster) {
            this.homeLinup.add(BoxPlayer(player, proSchedule, positionalRankings, week, year))
        }

        this.awayTeam = 0
        this.awayScore = 0.0
        this.awayLineup = mutableListOf()
        this.awayProjected = -1.0

        if ("away" in data.jsonObject) {
            this.awayTeam = data.jsonObject["away"]!!.jsonObject["teamId"].toString().toInt()
            if ("totalPointsLive" in data.jsonObject["away"].toString()) {
                this.awayScore = data.jsonObject["away"]!!.jsonObject["totalPointsLive"].toString().toDouble().round(2)
                this.awayProjected =
                    data.jsonObject["away"]!!.jsonObject.getOrDefault("totalProjectedPointsLive", -1).toString()
                        .toDouble()
                        .round(2)
            } else {

                this.awayScore =
                    data.jsonObject["away"]!!.jsonObject["totalPoints"].toString()
                        .toDouble()
            }
        }

        this.awayRoster =
            data.jsonObject["away"]!!.jsonObject["rosterForCurrentScoringPeriod"]!!.jsonObject["entries"]!!.jsonArray


        for (player in awayRoster) {
            this.awayLineup.add(BoxPlayer(player, proSchedule, positionalRankings, week, year))
        }
    }

}
