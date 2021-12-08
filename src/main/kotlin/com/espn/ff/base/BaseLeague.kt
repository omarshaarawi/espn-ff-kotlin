package com.espn.ff.base

import com.espn.ff.model.Team
import com.espn.ff.service.EspnRequests
import kotlin.properties.Delegates
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import mu.KLogger
import mu.KotlinLogging

open class BaseLeague(leagueId: Int, year: Int, swid: String, espnS2: String) {

    lateinit var settings: BaseSettings
    var currentWeek by Delegates.notNull<Int>()
    private var firstScoringPeriod by Delegates.notNull<Int>()
    var scoringPeriodId by Delegates.notNull<Int>()
    var currentMatchupPeriod by Delegates.notNull<Int>()
    private val logger: KLogger = KotlinLogging.logger {}
    private val leagueId: Int
    var year: Int
    var teams: MutableList<Team> = mutableListOf()
    private var draft: List<Any>
    var playerMap: MutableMap<Any, Any>
    var espnRequest: EspnRequests


    init {
        this.leagueId = leagueId
        this.year = year
        this.teams = emptyList<Team>().toMutableList()
        this.draft = emptyList()
        this.playerMap = mutableMapOf()
        this.espnRequest = EspnRequests(year, leagueId, swid, espnS2, logger)
    }

    open fun fetchLeague(): JsonObject? {
        val data = espnRequest.getLeague()
        this.currentMatchupPeriod = data?.get("status")!!.jsonObject["currentMatchupPeriod"]!!.toString().toInt()
        this.scoringPeriodId = data["scoringPeriodId"]!!.toString().toInt()
        this.firstScoringPeriod = data["status"]?.jsonObject?.get("firstScoringPeriod")!!.toString().toInt()
        if (year < 2018) {
            this.currentWeek = data["scoringPeriodId"]!!.toString().toInt()
        } else {
            if (this.scoringPeriodId.toString().toInt() <= data["status"]?.jsonObject?.get("finalScoringPeriod")
                    .toString().toInt()
            ) {
                this.currentWeek = this.scoringPeriodId
            } else {
                this.currentWeek = data["status"]?.jsonObject?.get("finalScoringPeriod")!!.toString().toInt()
            }

        }

        this.settings = data["settings"]?.let { BaseSettings(it) }!!

        return data

    }

    open fun fetchTeams(data: JsonObject) {
        teams.clear()
        val teamData = data["teams"]
        val members = data["members"]
        val schedule = data["schedule"]
        val seasonId = data["seasonId"]
        var newMember: JsonElement? = null

        val teamRoster = emptyMap<JsonElement?, JsonElement?>().toMutableMap()

        for (team in data["teams"] as JsonArray) {
            teamRoster[team.jsonObject["id"]] = team.jsonObject["roster"]
        }


        for (team in teamData!!.jsonArray) {
            for (member in members!!.jsonArray) {
                if (member.jsonObject["id"] == team.jsonObject["owners"]!!.jsonArray[0]) {
                    newMember = member
                    break
                }
            }


            val key = team.jsonObject["id"]
            val roster: JsonElement? = teamRoster[key]
            this.teams.add(
                Team(
                    teamData = team,
                    roster = roster!!,
                    member = newMember!!,
                    scheduleData = schedule,
                    year = seasonId.toString().toInt()
                )
            )
        }

        this.teams.sortBy { it.teamId }

    }


    fun fetchPlayers() {
        val data = this.espnRequest.getProPlayers()

        for (player in data.jsonArray) {
            this.playerMap[player.jsonObject["id"].toString()] = player.jsonObject["fullName"].toString()

            if (player.jsonObject["fullName"].toString() !in this.playerMap) {
                this.playerMap[player.jsonObject["fullName"].toString()] = player.jsonObject["id"].toString()
            }
        }
    }

    fun getProSchedule(scoringPeriodId: Int? = null): MutableMap<Any, Any> {
        val data = this.espnRequest.getProSchedules()

        val proTeams = data.jsonObject["settings"]!!.jsonObject["proTeams"]
        val proTeamSchedule = mutableMapOf<Any, Any>()

        for (team in proTeams!!.jsonArray) {
            val proGame = team.jsonObject["proGamesByScoringPeriod"]
            if (team.jsonObject["id"].toString()
                    .toInt() != 0 && (scoringPeriodId.toString() in proGame!!.jsonObject.keys && proGame.jsonObject[scoringPeriodId.toString()] != null)
            ) {
                val gameData = proGame.jsonObject[scoringPeriodId.toString()]!!.jsonArray[0]
                proTeamSchedule[team.jsonObject["id"].toString()] =
                    if (team.jsonObject["id"].toString() == gameData.jsonObject["awayProTeamId"].toString()) {
                        mapOf(gameData.jsonObject["homeProTeamId"] to gameData.jsonObject["date"])
                    } else {
                        mapOf(gameData.jsonObject["awayProTeamId"] to gameData.jsonObject["date"])
                    }
            }
        }
        return proTeamSchedule
    }

    open fun standings(): MutableList<Team> {
        val standings = teams.sortedBy {
            if (it.finalStanding.toString().toInt() != 0) {
                it.finalStanding.toString().toInt()
            } else {
                it.standing.toString().toInt()
            }
        } as MutableList<Team>
        return standings
    }
}