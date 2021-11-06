package com.espn.ff.model

import com.espn.ff.util.EspnConstants.ACTIVITY_MAP
import java.time.Instant
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject


class Activity(
    data: JsonElement,
    getTeamData: (teamId: Int) -> Team?,
    playerInfo: (name: String?, playerId: Int) -> Player?,
) {

    val date: Instant
    val actions: MutableList<List<Any>> = mutableListOf()

    init {
        this.date = Instant.ofEpochMilli(data.jsonObject["date"].toString().toLong())
        for (msg in data.jsonObject["messages"]!!.jsonArray) {
            var team: Team?
            var action = "UNKNOWN"
            var player: Player? = null
            var bidAmount = 0
            val msgId = msg.jsonObject["messageTypeId"].toString().toInt()
            if (msgId == 244) {
                team = getTeamData(msg.jsonObject["from"].toString().toInt())
            } else if (msgId == 239) {
                team = getTeamData(msg.jsonObject["for"].toString().toInt())
            } else {
                team = getTeamData(msg.jsonObject["to"].toString().toInt())
            }
            if (msgId in ACTIVITY_MAP) {
                action = ACTIVITY_MAP[msgId].toString()
            }
            if (action == "WAIVER ADDED") {
                bidAmount = msg.jsonObject["from"].toString().toInt()
            }

            if (team != null) {
                for (teamPlayer in team.roster) {
                    if (teamPlayer.playerId == msg.jsonObject["targetId"]) {
                        player = teamPlayer
                        break
                    }
                }
            }
            if (player == null) {
                player = playerInfo(null, msg.jsonObject["targetId"].toString().toInt())
            }

            this.actions.add(listOf(team!!, action, player!!, bidAmount))
        }
    }

}
