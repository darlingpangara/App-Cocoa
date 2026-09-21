package com.example.model

import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.floor
import kotlin.random.Random

data class TreeSample(
    val code: String,
    var pods: Int
)

object TreeSampleHelper {
    fun parseJson(jsonStr: String): List<TreeSample> {
        val list = mutableListOf<TreeSample>()
        try {
            val jsonArray = JSONArray(jsonStr)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(
                    TreeSample(
                        code = obj.optString("code", "P.${(65 + i).toChar()}"),
                        pods = obj.optInt("pods", 5)
                    )
                )
            }
        } catch (e: Exception) {
            // Fallback default
        }
        return list
    }

    fun toJson(list: List<TreeSample>): String {
        val jsonArray = JSONArray()
        for (item in list) {
            val obj = JSONObject()
            obj.put("code", item.code)
            obj.put("pods", item.pods)
            jsonArray.put(obj)
        }
        return jsonArray.toString()
    }

    /**
     * Generate tree sample codes P.A, P.B, P.C...
     */
    fun getTreeCode(index: Int): String {
        if (index < 26) {
            return "P.${(65 + index).toChar()}"
        }
        val first = (65 + (index / 26) - 1).toChar()
        val second = (65 + (index % 26)).toChar()
        return "P.$first$second"
    }

    /**
     * Formula Excel user request:
     * =ROUNDDOWN((jumlah produksi / (0.07 * total jumlah pohon)) * jumlah pohon sampel)
     * Satuan: Buah.
     * Kemudian mendistribusikan total buah tersebut ke masing-masing pohon sampel (P.A, P.B, ...)
     * bervariasi antara 2-17 buah per pohon.
     */
    fun generateDynamicSamples(
        sampleCount: Int,
        targetProductionKg: Double,
        totalTrees: Int
    ): List<TreeSample> {
        val validSampleCount = sampleCount.coerceAtLeast(1)
        val validTotalTrees = totalTrees.coerceAtLeast(1)
        val validProduction = targetProductionKg.coerceAtLeast(10.0)

        // Rumus Excel: =ROUNDDOWN((jumlah produksi / (0.07 * total jumlah pohon)) * jumlah pohon sampel)
        val denominator = 0.07 * validTotalTrees
        val rawTotalPods = if (denominator > 0) {
            floor((validProduction / denominator) * validSampleCount).toInt()
        } else {
            validSampleCount * 8
        }

        // Clamp total pods within reasonable agronomy range (2 to 17 pods per tree)
        val minTotal = validSampleCount * 2
        val maxTotal = validSampleCount * 17
        val targetTotalPods = rawTotalPods.coerceIn(minTotal, maxTotal)

        // Distribute dynamically across sample trees (2-17 pods each)
        val podCounts = IntArray(validSampleCount) { 2 }
        var remainingPods = targetTotalPods - (validSampleCount * 2)

        // Random distribution variation
        val rng = Random(System.currentTimeMillis())
        while (remainingPods > 0) {
            val eligibleIndices = (0 until validSampleCount).filter { podCounts[it] < 17 }
            if (eligibleIndices.isEmpty()) break
            val chosen = eligibleIndices[rng.nextInt(eligibleIndices.size)]
            podCounts[chosen]++
            remainingPods--
        }

        // Return the list with codes P.A, P.B, P.C...
        return (0 until validSampleCount).map { idx ->
            TreeSample(
                code = getTreeCode(idx),
                pods = podCounts[idx]
            )
        }
    }
}
