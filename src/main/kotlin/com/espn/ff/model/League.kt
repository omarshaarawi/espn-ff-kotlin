package com.espn.ff.model

import com.espn.ff.base.BaseLeague
import com.espn.ff.util.EspnConstants.ACTIVITY_MAP
import com.espn.ff.util.EspnConstants.POSITION_MAP
import com.espn.ff.util.EspnUtils
import com.google.gson.Gson
import kotlin.math.abs
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject

class League(leagueId: Int, year: Int, swid: String, espnS2: String) : BaseLeague(leagueId, year, swid, espnS2) {

    private lateinit var nflWeek: JsonElement

    init {
        this.fetchLeague()
    }

    override fun fetchLeague(): JsonObject {
        val data = super.fetchLeague()

        this.nflWeek = data!!["status"]!!.jsonObject["latestScoringPeriod"]!!
        this.fetchPlayers()
        this.fetchTeams(data)
        return data
    }

    override fun fetchTeams(data: JsonObject) {
        super.fetchTeams(data)

        for (team in this.teams) {
            team.schedule.forEachIndexed { week, matchup ->
                for (opponent in this.teams) {
                    when (matchup) {
                        is Team -> team.schedule[week] = opponent

                        else -> if (matchup.toString().toInt() == opponent.teamId) {
                            team.schedule[week] = opponent

                        }
                    }
                }
            }
        }
        for (team in this.teams) {
            team.schedule.forEachIndexed { week, opponent ->
                val mov =
                    team.scores[week].toString().toDouble() - (opponent as Team).scores[week].toString().toDouble()
                team.mov.add(mov)
            }
        }

    }

    fun getPositionalRatings(week: Int): MutableMap<Int, Any> {
        val views = listOf("mPositionalRatings")
        val params = mapOf("scoringPeriodId" to week.toString())

        val data = this.espnRequest.leagueGet(params = params, views = views)
        val ratings = data.jsonObject["positionAgainstOpponent"]!!.jsonObject["positionalRatings"] as JsonObject

        val positionalRatings = mutableMapOf<Int, Any>()

        ratings.entries.forEachIndexed { pos, rating ->
            val teamRating = mutableMapOf<Int, Any>()
            (rating.value.jsonObject["ratingsByOpponent"] as JsonObject).entries.forEachIndexed { team, data ->
                teamRating[team] = data.value.jsonObject["rank"].toString().toDouble()
            }
            positionalRatings[pos] = teamRating
        }
        return positionalRatings
    }


    fun refresh() {
        val data = super.fetchLeague()
        this.nflWeek = data!!["status"]!!.jsonObject["latestScoringPeriod"]!!
        this.fetchTeams(data)
    }

    fun loadRosterWeek(week: Int) {
        val views = listOf("mRoster")
        val params = mapOf("scoringPeriodId" to week.toString())

        val data = this.espnRequest.leagueGet(params = params, views = views)

        val teamRoster = mutableMapOf<String, JsonElement>()

        for (team in data.jsonObject["teams"]!!.jsonArray) {
            teamRoster[team.jsonObject["id"].toString()] = team.jsonObject["roster"] as JsonElement
        }

        for (team in this.teams) {
            val roster: JsonElement = teamRoster[team.teamId.toString()] as JsonElement
            team.fetchRoster(roster, this.year)
        }
    }

    override fun standings(): MutableList<Team> {
        val standings = teams.sortedBy {
            if (it.finalStanding.toString().toInt() != 0) {
                it.finalStanding.toString().toInt()
            } else {
                it.standing.toString().toInt()
            }
        } as MutableList<Team>
        return standings
    }

    fun topScorer(): Team {
        return teams.sortedBy { it.pointsFor.toString().toDouble() }.reversed()[0]
    }

    fun leastScorer(): Team {
        return teams.sortedBy { it.pointsFor.toString().toDouble() }[0]
    }

    fun mostPointsAgainst(): Team {
        val leastPf = teams.sortedBy { it.pointsAgainst.toString().toDouble() }.reversed()
        return leastPf[0]
    }

    fun closestScore(matchups: List<BoxScore>): Triple<String, String, Double> {
        var closestScore = 500.0
        var closeWinner = ""
        var closeLoser = ""

        for (i in matchups) {
            if (abs(i.awayScore - i.homeScore) < closestScore) {
                closestScore = abs(i.awayScore - i.homeScore)
                if ((i.awayScore - i.homeScore) < 0) {
                    closeWinner = (i.homeTeam as Team).teamName
                    closeLoser = (i.awayTeam as Team).teamName
                } else {
                    closeWinner = (i.awayTeam as Team).teamName
                    closeLoser = (i.homeTeam as Team).teamName
                }
            }
        }

        return Triple(closeWinner, closeLoser, closestScore)
    }

    fun blowout(matchups: List<BoxScore>): Triple<String, String, Double> {

        var kingTeamName = ""
        var blownOutTeamName = ""
        var biggestBlowout = -500.0

        for (i in matchups) {
            if (abs(i.awayScore - i.homeScore) > biggestBlowout) {
                biggestBlowout = abs(i.awayScore - i.homeScore)
                if ((i.awayScore - i.homeScore) < 0) {
                    kingTeamName = (i.homeTeam as Team).teamName
                    blownOutTeamName = (i.awayTeam as Team).teamName
                } else {
                    kingTeamName = (i.awayTeam as Team).teamName
                    blownOutTeamName = (i.homeTeam as Team).teamName
                }
            }
        }
        return Triple(kingTeamName, blownOutTeamName, biggestBlowout)
    }

    fun topScoredWeek(): Pair<Any, Any> {
        val topWeekPoints = mutableListOf<Any>()
        val topScoredPairList = mutableListOf<Pair<Any, Any>>()

        for (team in this.teams) {
            topWeekPoints.add(team.scores[this.currentWeek.toString().toInt()]) // might remove -1
        }
        for ((i, j) in this.teams zip topWeekPoints) {
            topScoredPairList.add(Pair(i.teamName, j))
        }
        val topPair = topScoredPairList.sortedBy { it.second.toString().toDouble() }.reversed()
        return topPair[0]
    }

    fun leastScoredWeek(): Pair<Any, Any> {
        val leastWeekPoints = mutableListOf<Any>()
        val leastScoredPairList = mutableListOf<Pair<Any, Any>>()

        for (team in this.teams) {
            leastWeekPoints.add(team.scores[this.currentWeek.toString().toInt()])
        }
        for ((i, j) in this.teams zip leastWeekPoints) {
            leastScoredPairList.add(Pair(i.teamName, j))
        }
        val topPair = leastScoredPairList.sortedBy { it.second.toString().toDouble() }
        return topPair[0]
    }

    fun getTeamData(teamId: Int): Team? {
        for (team in this.teams) {
            if (teamId == team.teamId) {
                return team
            } else {
                continue
            }
        }
        return null
    }

    fun recentActivity(size: Int = 25, msgType: String = ""): MutableList<Any> {
        var msgTypes: List<Any> = listOf(178, 180, 179, 239, 181, 244)

        val activity = mutableListOf<Any>()
        if (msgType in ACTIVITY_MAP) {
            msgTypes = listOf(ACTIVITY_MAP[msgType]!!)
        }
        val views = listOf("kona_league_communication")

        val filters = mapOf(
            "topics" to mapOf(
                "filterType" to mapOf(
                    "value" to listOf(
                        "ACTIVITY_TRANSACTIONS"
                    )
                ), "limit" to size, "limitPerMessageSet" to mapOf(
                    "value" to 25
                ), "offset" to 0, "sortMessageDate" to mapOf(
                    "sortPriority" to 1, "sortAsc" to false
                ), "sortFor" to mapOf(
                    "sortPriority" to 2, "sortAsc" to false
                ), "filterIncludeMessageTypeIds" to mapOf(
                    "value" to msgTypes
                )
            )
        )

        val gson = Gson()


        val headers = mapOf(
            "x-fantasy-filter" to gson.toJson(filters).toString()
        )


        val data = this.espnRequest.leagueGet(
            extend = "/communication/", views = views, headers = headers
        ).jsonObject["topics"]!!.jsonArray
        for (topic in data) {
            activity.add(Activity(topic, this::getTeamData, this::playerInfo))
        }
        return activity
    }


    fun scoreboard(week: Int? = null): MutableList<Matchup> {
        var newWeek = week
        val matchups = mutableListOf<Matchup>()
        if (week == null) {
            newWeek = this.currentWeek.toString().toInt()
        }

        val views = listOf("mMatchupScore")

        val data = this.espnRequest.leagueGet(views = views)
        val schedule = data.jsonObject["schedule"]

        for (matchup in schedule!!.jsonArray) {
            if (matchup.jsonObject["matchupPeriodId"].toString().toInt() == newWeek) {
                matchups.add(Matchup(matchup))
            }
        }

        for (team in this.teams) {
            for (matchup in matchups) {
                if (matchup.homeTeam == team.teamId) {
                    matchup.homeTeam = team
                } else if (matchup.awayTeam == team.teamId) {
                    matchup.awayTeam = team
                }
            }
        }

        return matchups


    }

    fun boxScore(week: Int? = null): MutableList<BoxScore> {

        val boxData = mutableListOf<BoxScore>()
        if (this.year < 2019) {
            throw Exception()
        }

        var matchupPeriod = this.currentMatchupPeriod.toString().toInt()
        var scoringPeriod = this.currentWeek.toString().toInt()

        if (week != null && week <= this.currentWeek.toString().toInt()) {
            scoringPeriod = week
            for (matchupId in (this.settings.matchupPeriods as JsonObject).entries.toMutableList()) {
                if (week == (this.settings.matchupPeriods as JsonObject).entries.toMutableList()[matchupId.key.toInt()].key.toInt()) {
                    matchupPeriod = matchupId.key.toInt() + 1 // could be wrong
                    break
                }
            }
        }

        val views = listOf("mMatchupScore", "mScoreboard")
        val params = mapOf("scoringPeriodId" to scoringPeriod.toString())

        val filters = mapOf(
            "schedule" to mapOf(
                "filterMatchupPeriodIds" to mapOf(
                    "value" to listOf(matchupPeriod)
                )

            )
        )


        val gson = Gson()
        val headers = mapOf(
            "x-fantasy-filter" to gson.toJson(filters).toString()
        )

        val data = this.espnRequest.leagueGet(views = views, params = params, headers = headers)

        val schedule = data.jsonObject["schedule"]

        val proSchedule = this.getProSchedule(scoringPeriod)
        val positionalRankings = this.getPositionalRatings(scoringPeriod)


        for (matchup in schedule!!.jsonArray) {
            boxData.add(BoxScore(matchup, proSchedule, positionalRankings, scoringPeriod, this.year))
        }


        for (team in this.teams) {
            for (matchup in boxData) {
                if (matchup.homeTeam == team.teamId) {
                    matchup.homeTeam = team
                } else if (matchup.awayTeam == team.teamId) {
                    matchup.awayTeam = team
                }
            }
        }

        return boxData

    }

    fun powerRankings(week: Int? = null): List<Pair<Double, Team>> {
        var newWeek: Int = 0
        if (week == null || week <= 0 || week > this.currentWeek.toString().toInt()) {
            newWeek = this.currentWeek.toString().toInt()
        }

        val winMatrix = mutableListOf<MutableList<Int>>()
        val teamsSorted = this.teams.sortedBy { it.teamId }

        for (team in teamsSorted) {
            val wins = MutableList(teamsSorted.size) { 0 }
            val a = team.mov.subList(0, newWeek)
            val b = team.schedule.subList(0, newWeek)
            for ((mov, opponent) in a zip b) {
                val opp = teamsSorted.indexOf(opponent)
                if (mov.toString().toDouble() > 0) {
                    wins[opp] += 1
                }
            }
            winMatrix.add(wins)
        }
        val dominanceMatrix = EspnUtils.twoStepDominance(winMatrix)

        return EspnUtils.powerPoints(dominanceMatrix, teamsSorted, newWeek)
    }


    fun freeAgents(
        week: Int? = null,
        size: Int? = 50,
        position: String? = null,
        positionId: Int? = null,
    ): MutableList<BoxPlayer> {

        var newWeek = week
        if (this.year < 2019) {
            throw Exception()
        }
        if (week == null) {
            newWeek = this.currentWeek.toString().toInt()
        }

        val slotFilters = mutableListOf<Any>()

        if (position != null && position in POSITION_MAP) {
            slotFilters.add(POSITION_MAP[position]!!)
        }

        if (positionId != null) {
            slotFilters.add(positionId)
        }

        val views = listOf("kona_player_info")
        val params = mapOf("scoringPeriodId" to newWeek.toString())

        val filters = mapOf(
            "players" to mapOf(
                "filterStatus" to mapOf("value" to listOf("FREEAGENT", "WAIVERS")),
                "filterSlotIds" to mapOf("value" to slotFilters),
                "limit" to size,
                "sortPercOwned" to mapOf("sortPriority" to 1, "sortAsc" to "false"),
                "sortDraftRanks" to mapOf("sortPriority" to 100, "sortAsc" to "true", "value" to "STANDARD")
            )
        )

        val gson = Gson()
        val headers = mapOf(
            "x-fantasy-filter" to gson.toJson(filters).toString()
        )

        val data = this.espnRequest.leagueGet(views = views, params = params, headers = headers)

        val players = data.jsonObject["players"]!!.jsonArray

        val proSchedule = this.getProSchedule(newWeek)
        val positionalRankings = this.getPositionalRatings(newWeek!!)

        val boxPlayerList = mutableListOf<BoxPlayer>()

        for (player in players) {
            boxPlayerList.add(BoxPlayer(player, proSchedule, positionalRankings, newWeek, this.year))
        }

        return boxPlayerList

    }

    private fun playerInfo(name: String? = null, playerId: Int? = null): Player? {
        var tempPlayerId: Int? = playerId
        if (!name.isNullOrBlank()) {
            tempPlayerId = this.playerMap[name].toString().toInt()
        }

        val views = listOf("kona_playercard")
        val filters = mapOf(
            "players" to mapOf(
                "filterIds" to mapOf(
                    "value" to listOf(tempPlayerId),
                ), "filterStatsForTopScoringPeriodIds" to mapOf(
                    "value" to 16, "additionalValue" to listOf("00${this.year}", "10${this.year}")
                )

            )
        )
        val gson = Gson()

        val headers = mapOf(
            "x-fantasy-filter" to gson.toJson(filters).toString()
        )

        val data = this.espnRequest.leagueGet(views = views, headers = headers)

        if (data.jsonObject["players"]!!.jsonArray.size > 0) {
            return Player(data.jsonObject["players"]!!.jsonArray[0], this.year)
        }
        return null
    }
}