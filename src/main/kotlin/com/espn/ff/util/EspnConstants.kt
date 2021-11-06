package com.espn.ff.util

object EspnConstants {
    const val FANTASY_BASE_ENDPOINT = "https://fantasy.espn.com/apis/v3/games/"


    val PRO_TEAM_MAP = mapOf(

        0 to "None",
        1 to "ATL",
        2 to "BUF",
        3 to "CHI",
        4 to "CIN",
        5 to "CLE",
        6 to "DAL",
        7 to "DEN",
        8 to "DET",
        9 to "GB",
        10 to "TEN",
        11 to "IND",
        12 to "KC",
        13 to "OAK",
        14 to "LAR",
        15 to "MIA",
        16 to "MIN",
        17 to "NE",
        18 to "NO",
        19 to "NYG",
        20 to "NYJ",
        21 to "PHI",
        22 to "ARI",
        23 to "PIT",
        24 to "LAC",
        25 to "SF",
        26 to "SEA",
        27 to "TB",
        28 to "WSH",
        29 to "CAR",
        30 to "JAX",
        33 to "BAL",
        34 to "HOU"
    )
    val POSITION_MAP = mapOf(
        0 to "QB",
        1 to "TQB",
        2 to "RB",
        3 to "RB/WR",
        4 to "WR",
        5 to "WR/TE",
        6 to "TE",
        7 to "OP",
        8 to "DT",
        9 to "DE",
        10 to "LB",
        11 to "DL",
        12 to "CB",
        13 to "S",
        14 to "DB",
        15 to "DP",
        16 to "D/ST",
        17 to "K",
        18 to "P",
        19 to "HC",
        20 to "BE",
        21 to "IR",
        22 to "",
        23 to "RB/WR/TE",
        24 to "ER",
        25 to "Rookie",
        "QB" to 0,
        "RB" to 2,
        "WR" to 4,
        "TE" to 6,
        "D/ST" to 16,
        "K" to 17,
        "FLEX" to 23,
        "DT" to 8,
        "DE" to 9,
        "LB" to 10,
        "DL" to 11,
        "CB" to 12,
        "S" to 13,
        "DB" to 14,
        "DP" to 15,
        "HC" to 19
    )


    val ACTIVITY_MAP = mapOf(
        178 to "FA ADDED",
        180 to "WAIVER ADDED",
        179 to "DROPPED",
        181 to "DROPPED",
        239 to "DROPPED",
        244 to "TRADED",
        "FA" to 178,
        "WAIVER" to 180,
        "TRADED" to 244

    )

    val PLAYER_STATS_MAP = mapOf(
        3 to "passingYards",
        4 to "passingTouchdowns",

        19 to "passing2PtConversions",
        20 to "passingInterceptions",

        24 to "rushingYards",
        25 to "rushingTouchdowns",
        26 to "rushing2PtConversions",

        42 to "receivingYards",
        43 to "receivingTouchdowns",
        44 to "receiving2PtConversions",
        53 to "receivingReceptions",

        72 to "lostFumbles",

        74 to "madeFieldGoalsFrom50Plus",
        77 to "madeFieldGoalsFrom40To49",
        80 to "madeFieldGoalsFromUnder40",
        85 to "missedFieldGoals",
        86 to "madeExtraPoints",
        88 to "missedExtraPoints",

        89 to "defensive0PointsAllowed",
        90 to "defensive1To6PointsAllowed",
        91 to "defensive7To13PointsAllowed",
        92 to "defensive14To17PointsAllowed",

        93 to "defensiveBlockedKickForTouchdowns",
        95 to "defensiveInterceptions",
        96 to "defensiveFumbles",
        97 to "defensiveBlockedKicks",
        98 to "defensiveSafeties",
        99 to "defensiveSacks",

        101 to "kickoffReturnTouchdown",
        102 to "puntReturnTouchdown",
        103 to "fumbleReturnTouchdown",
        104 to "interceptionReturnTouchdown",

        123 to "defensive28To34PointsAllowed",
        124 to "defensive35To45PointsAllowed",

        129 to "defensive100To199YardsAllowed",
        130 to "defensive200To299YardsAllowed",
        132 to "defensive350To399YardsAllowed",
        133 to "defensive400To449YardsAllowed",
        134 to "defensive450To499YardsAllowed",
        135 to "defensive500To549YardsAllowed",
        136 to "defensiveOver550YardsAllowed",
        140 to "puntsInsideThe10",
        141 to "puntsInsideThe20",  // PT20
        148 to "puntAverage44.0+",  // PTA44
        149 to "puntAverage42.0-43.9",  //PTA42
        150 to "puntAverage40.0-41.9",  //PTA40

        // Head Coach stats
        161 to "25+pointsWinMargin",  //WM25
        162 to "20-24pointWinMargin",  //WM20
        163 to "15-19pointWinMargin",  //WM15
        164 to "10-14pointWinMargin",  //WM10
        165 to "5-9pointWinMargin",  // WM5
        166 to "1-4pointWinMargin",  // WM1

        155 to "TeamWin",  // TW

        171 to "20-24pointLossMargin",  // LM20
        172 to "25+pointLossMargin",  // LM25
    )

}