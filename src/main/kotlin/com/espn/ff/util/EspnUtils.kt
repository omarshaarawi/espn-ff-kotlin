package com.espn.ff.util

import com.espn.ff.model.Team
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement


object EspnUtils {
    fun jsonParsing(data: JsonElement, key: String): Any {

        val arr: MutableList<JsonElement> =
            emptyList<JsonElement>().toMutableList()

        fun extract(
            data: JsonElement,
            arr: MutableList<JsonElement>,
            key: String,
        ): MutableList<JsonElement> {

            if (data is Map<*, *>) {
                for ((k, v) in data) {
                    if (v is Map<*, *> || (v is List<*> && v.isNotEmpty() && v[0] is List<*>)) {
                        extract(v as JsonElement, arr, key)
                    } else if (k == key) {
                        arr.add(v as JsonElement)
                    }
                }
            } else if (data is JsonArray) {
                for (item in data) {
                    extract(item, arr, key)
                }

            }

            return arr

        }

        val results = extract(data, arr, key)
        return if (results.size > 0) results[0] else results
    }

    fun Double.round(decimals: Int): Double {
        var multiplier = 1.0
        repeat(decimals) { multiplier *= 10 }
        return kotlin.math.round(this * multiplier) / multiplier
    }

    fun twoStepDominance(winMatrix: MutableList<MutableList<Int>>): MutableList<Int> {
        val matrix = addMatrix(squareMatrix(winMatrix), winMatrix)
        val result = mutableListOf<Int>()
        for (x in matrix) {
            result.add(x.sum())
        }

        return result
    }

    private fun squareMatrix(winMatrix: MutableList<MutableList<Int>>): MutableList<MutableList<Int>> {
        val result = MutableList(winMatrix.size) { MutableList(winMatrix.size) { 0 } }

        for (i in winMatrix.indices) {
            for (j in winMatrix.indices) {
                for (k in winMatrix.indices) {
                    result[i][j] += winMatrix[i][k] * winMatrix[k][j]
                }
            }
        }

        return result
    }

    private fun addMatrix(
        x: MutableList<MutableList<Int>>,
        y: MutableList<MutableList<Int>>
    ): MutableList<MutableList<Int>> {
        val result = MutableList(x.size) { MutableList(x.size) { 0 } }

        for (i in x.indices) {
            for (j in x.indices) {
                result[i][j] += x[i][j] * y[i][j]
            }
        }

        return result

    }

    fun powerPoints(dominanceMatrix: MutableList<Int>, teams: List<Team>, week: Int): List<Pair<Double, Team>> {
        val powerPoints = mutableListOf<Double>()
        val powerPairList = mutableListOf<Pair<Double, Team>>()

        for ((i, team) in dominanceMatrix zip teams) {
            val avgScore = team.scores.subList(0, week).sum() / week
            val avgMov = team.mov.subList(0, week).sum() / week

            val power = "%.2f".format((i * .8) + (avgScore * 0.15) + (avgMov * 0.05))

            powerPoints.add(power.toDouble())

        }

        for ((i, j) in powerPoints zip teams) {
            powerPairList.add(Pair(i, j))
        }

        return powerPairList.sortedBy { it.first.toString().toDouble() }.reversed()

    }

}