package com.espn.ff.model

import com.espn.ff.util.EspnConstants.POSITION_MAP
import com.espn.ff.util.EspnConstants.PRO_TEAM_MAP
import java.util.Date
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject

class BoxPlayer(
    data: JsonElement,
    proSchedule: MutableMap<Any, Any>,
    positionalRankings: MutableMap<Int, Any>,
    week: Int,
    year: Int,
) : Player(data, year) {

    lateinit var projectedBreakdown: Any
    var projectedPoints = 0.0
    lateinit var pointsBreakdown: Any
    var points = 0.0
    var proOpponent: String
    var slotPosition: String
    var proPosRank: Double
    var gamePlayed: Int

    init {
        this.slotPosition = "FA"
        this.proOpponent = "None"
        this.proPosRank = 0.0
        this.gamePlayed = 100

        if ("lineupSlotId" in data.jsonObject) {
            this.slotPosition = POSITION_MAP[data.jsonObject["lineupSlotId"].toString().toInt()].toString()
        }

        val player =
            if ("playerPoolEntry" in data.jsonObject) {
                data.jsonObject["playerPoolEntry"]!!.jsonObject["player"]
            } else {
                data.jsonObject["player"]
            }

        if (player!!.jsonObject["proTeamId"].toString() in proSchedule) {
            val oppID =
                (proSchedule[player.jsonObject["proTeamId"].toString()] as Map<*, *>).keys.first().toString().toInt()
            val date = (proSchedule[player.jsonObject["proTeamId"].toString()] as Map<*, *>).values.first().toString()
            val today = Date(Date().time)
            val s = Date(date.toLong() + 3 * 60 * 60 * 1000)
            this.gamePlayed = if (today > s) {
                100
            } else {
                0
            }


            val posId = player.jsonObject["defaultPositionId"]

            if (posId.toString().toInt() in positionalRankings) {
                this.proOpponent = PRO_TEAM_MAP[oppID].toString()
                this.proPosRank = if ((positionalRankings[posId.toString().toInt()] as Map<*, *>).contains(oppID)) {
                    (positionalRankings[posId.toString().toInt()] as Map<*, *>).get(oppID).toString().toDouble()
                } else {
                    0.0
                }
            }


            val stats = this.stats.getOrDefault(week, emptyMap<Any, Any>()) as Map<*, *>
            this.points = stats.getOrDefault("points", 0.0).toString().toDouble()
            this.projectedPoints = stats.getOrDefault("projected_points", 0.0).toString().toDouble()
            this.pointsBreakdown = stats.getOrDefault("breakdown", 0)!!
            this.projectedBreakdown = stats.getOrDefault("projected_breakdown", 0)!!
        }
    }
}



