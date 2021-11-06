package com.espn.ff.model

import com.espn.ff.util.EspnConstants.PLAYER_STATS_MAP
import com.espn.ff.util.EspnConstants.POSITION_MAP
import com.espn.ff.util.EspnConstants.PRO_TEAM_MAP
import com.espn.ff.util.EspnUtils.jsonParsing
import com.espn.ff.util.EspnUtils.round
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

open class Player(data: JsonElement, year: Int) {
    var projectedTotalPoints: Any
    var totalPoints: Any
    var injured: String
    lateinit var position: Any
    var stats: MutableMap<Int, MutableMap<String, Any>>
    var injuryStatus: String
    var proTeam: String?
    var acquisitionType: String
    lateinit var eligibleSlots: List<Any?>

    var posRank: Int?
    var playerId: JsonElement
    var name: String

    init {
        this.name = jsonParsing(data, "fullName").toString().replace("\"", "")
        this.playerId = jsonParsing(data, "id") as JsonElement
        this.posRank = jsonParsing(data, "positionalRanking").toString().toIntOrNull()
        for (pos in (jsonParsing(data, "eligibleSlots") as JsonElement).jsonArray) {
            this.eligibleSlots = listOf(POSITION_MAP[pos.toString().toInt()])
        }
        this.acquisitionType = jsonParsing(data, "acquisitionType").toString()
        this.proTeam = PRO_TEAM_MAP[jsonParsing(data, "proTeamId").toString().toInt()]
        this.injuryStatus = jsonParsing(data, "injuryStatus").toString().replace("\"", "");
        this.stats = mutableMapOf()


        for (pos in (jsonParsing(data, "eligibleSlots") as JsonElement).jsonArray) {
            if ((pos.toString().toInt() != 25 && !POSITION_MAP[pos.toString()
                    .toInt()].toString().contains("/")) || "/" in this.name
            ) {
                this.position = POSITION_MAP[pos.toString().toInt()]!!
                break
            }
        }

        val player = if ("playerPoolEntry" in data.jsonObject) {
            data.jsonObject["playerPoolEntry"]!!.jsonObject["player"]

        } else {
            data.jsonObject["player"]!!
        } as JsonObject

        this.injuryStatus = player.getOrDefault("injuryStatus", this.injuryStatus).toString()
        this.injured = player["injured"].toString()

        val playerStats = player.jsonObject["stats"]
        val breakdown: MutableMap<String?, Double> = mutableMapOf()

        for (stats in playerStats!!.jsonArray) {
            if (stats.jsonObject["seasonId"].toString().toInt() != year) {
                continue
            }
            val statsBreakdown = if (stats.jsonObject["stats"] != null) {
                stats.jsonObject["stats"]
            } else {
                stats.jsonObject["appliedStats"]
            }

            if (statsBreakdown != null) {
                for ((k, v) in statsBreakdown.jsonObject.entries) {
                    breakdown[PLAYER_STATS_MAP.getOrDefault(k.toInt(), k)] = v.jsonPrimitive.toString().toDouble()
                }
            }
            val points = stats.jsonObject["appliedTotal"].toString().toDouble().round(2)
            val scoringPeriod = stats.jsonObject["scoringPeriodId"]
            val statSource = stats.jsonObject["statSourceId"]
            val (points_type, breakdown_type) = if (statSource.toString().toInt() == 0) {
                Pair("points", "breakdown")
            } else {
                Pair("projected_points", "projected_breakdown")
            }

            if (this.stats[scoringPeriod.toString().toInt()]?.isNotEmpty() == true) {
                this.stats.getOrPut(scoringPeriod.toString().toInt()) { mutableMapOf() }[points_type] = points
                this.stats.getOrPut(scoringPeriod.toString().toInt()) { mutableMapOf() }[breakdown_type] = breakdown

            } else {
                this.stats[scoringPeriod.toString().toInt()] =
                    mutableMapOf(points_type to points, breakdown_type to breakdown)

            }

        }

        this.totalPoints = this.stats.getOrDefault(0, emptyMap()).getOrDefault("points", 0)
        this.projectedTotalPoints = this.stats.getOrDefault(0, emptyMap()).getOrDefault("projected_points", 0)


    }

}
