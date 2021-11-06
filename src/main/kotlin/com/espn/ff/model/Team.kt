package com.espn.ff.model

import com.espn.ff.util.EspnUtils.round
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject

class Team(
    teamData: JsonElement,
    roster: JsonElement,
    member: JsonElement,
    scheduleData: JsonElement?,
    year: Int,
) {
    var outcomes: MutableList<Any>
    var scores: MutableList<Double>
    var schedule: MutableList<Any>
    var roster: MutableList<Player>
    var logoUrl: String
    var finalStanding: JsonElement?
    var standing: JsonElement?
    var streakType: JsonElement?
    var streakLength: Int
    var mov: MutableList<Double>
    var owner: String
    var pointsFor: JsonElement?
    var ties: Int
    var losses: Int
    var pointsAgainst: Double
    var divisionId: JsonElement
    var divisionName: String
    var wins: Int
    var teamName: String
    var teamAbbrev: String
    val teamId: Int


    init {
        this.teamId = teamData.jsonObject["id"].toString().toInt()
        this.teamAbbrev = teamData.jsonObject["abbrev"].toString()
        this.teamName = "${teamData.jsonObject["location"]} ${teamData.jsonObject["nickname"]}"
        this.divisionId = teamData.jsonObject["divisionId"]!!
        this.divisionName = ""
        this.wins = teamData.jsonObject["record"]!!.jsonObject["overall"]!!.jsonObject["wins"].toString().toInt()
        this.losses = teamData.jsonObject["record"]!!.jsonObject["overall"]!!.jsonObject["losses"].toString().toInt()
        this.ties = teamData.jsonObject["record"]!!.jsonObject["overall"]!!.jsonObject["ties"].toString().toInt()
        this.pointsFor = teamData.jsonObject["record"]!!.jsonObject["overall"]!!.jsonObject["pointsFor"]
        this.pointsAgainst =
            teamData.jsonObject["record"]!!.jsonObject["overall"]!!.jsonObject["pointsAgainst"].toString().toDouble()
                .round(2)
        this.owner = "${member.jsonObject["firstName"]} ${member.jsonObject["lastName"]}"
        this.streakLength =
            teamData.jsonObject["record"]!!.jsonObject["overall"]!!.jsonObject["streakLength"].toString().toInt()
        this.streakType = teamData.jsonObject["record"]!!.jsonObject["overall"]!!.jsonObject["streakType"]
        this.standing = teamData.jsonObject["playoffSeed"]
        this.finalStanding = teamData.jsonObject["rankCalculatedFinal"]
        if (teamData.jsonObject.contains("logo")) {
            this.logoUrl = teamData.jsonObject["logo"].toString()
        } else {
            this.logoUrl = ""
        }
        this.roster = mutableListOf()
        this.schedule = mutableListOf()
        this.scores = mutableListOf()
        this.outcomes = mutableListOf()
        this.mov = mutableListOf()
        this.fetchSchedule(scheduleData)
        this.fetchRoster(roster, year)


    }

    fun fetchRoster(roster: JsonElement, year: Int) {
        val rosterteam = roster.jsonObject["entries"]!!.jsonArray

        for (player in rosterteam) {
            this.roster.add(Player(player, year = year))
        }
    }

    private fun fetchSchedule(scheduleData: JsonElement?) {
        for (matchup in scheduleData!!.jsonArray) {
            if (matchup.jsonObject.contains("away")) {
                if (matchup.jsonObject["away"]!!.jsonObject["teamId"].toString().toInt() == this.teamId) {
                    val score = matchup.jsonObject["away"]!!.jsonObject["totalPoints"]
                    val opponentId = matchup.jsonObject["home"]!!.jsonObject["teamId"]
                    matchup.jsonObject["winner"]?.let { this.outcomes.add(it) }
                    this.scores.add(score!!.toString().toDouble())
                    this.schedule.add(opponentId!!)
                } else if (matchup.jsonObject["home"]!!.jsonObject["teamId"].toString().toInt() == this.teamId) {
                    val score = matchup.jsonObject["home"]!!.jsonObject["totalPoints"]
                    val opponentId = matchup.jsonObject["away"]!!.jsonObject["teamId"]
                    matchup.jsonObject["winner"]?.let { this.outcomes.add(it) }
                    this.scores.add(score!!.toString().toDouble())
                    this.schedule.add(opponentId!!)
                }
            } else if (matchup.jsonObject["home"]!!.jsonObject["teamId"].toString().toInt() == this.teamId) {
                val score = matchup.jsonObject["home"]!!.jsonObject["totalPoints"]
                val opponentId = matchup.jsonObject["home"]!!.jsonObject["teamId"]
                matchup.jsonObject["winner"]?.let { this.outcomes.add(it) }
                this.scores.add(score!!.toString().toDouble())
                this.schedule.add(opponentId!!)

            }
        }
    }
}