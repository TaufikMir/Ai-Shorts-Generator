package com.example.data.model

import org.json.JSONArray
import org.json.JSONObject

data class TimestampedSegment(
    val id: Int,
    val startTimeSeconds: Int,
    val endTimeSeconds: Int,
    val speaker: String,
    val text: String,
    val confidence: Float = 0.98f
) {
    fun formatTimestamp(): String {
        val startM = startTimeSeconds / 60
        val startS = startTimeSeconds % 60
        val endM = endTimeSeconds / 60
        val endS = endTimeSeconds % 60
        return String.format("%02d:%02d - %02d:%02d", startM, startS, endM, endS)
    }

    fun toJsonObject(): JSONObject = JSONObject().apply {
        put("id", id)
        put("startTimeSeconds", startTimeSeconds)
        put("endTimeSeconds", endTimeSeconds)
        put("speaker", speaker)
        put("text", text)
        put("confidence", confidence.toDouble())
    }

    companion object {
        fun fromJsonObject(json: JSONObject): TimestampedSegment = TimestampedSegment(
            id = json.optInt("id", 0),
            startTimeSeconds = json.optInt("startTimeSeconds", 0),
            endTimeSeconds = json.optInt("endTimeSeconds", 0),
            speaker = json.optString("speaker", "Speaker"),
            text = json.optString("text", ""),
            confidence = json.optDouble("confidence", 0.98).toFloat()
        )

        fun parseSegmentsJson(jsonStr: String): List<TimestampedSegment> {
            val list = mutableListOf<TimestampedSegment>()
            if (jsonStr.isBlank()) return list
            try {
                val array = JSONArray(jsonStr)
                for (i in 0 until array.length()) {
                    list.add(fromJsonObject(array.getJSONObject(i)))
                }
            } catch (e: Exception) {
                // Fallback for empty/malformed
            }
            return list
        }

        fun toJsonString(segments: List<TimestampedSegment>): String {
            val array = JSONArray()
            segments.forEach { array.put(it.toJsonObject()) }
            return array.toString()
        }
    }
}

enum class TranscriptionWorkerStage(val stepNumber: Int, val displayName: String) {
    QUEUED(1, "Queued in Background"),
    AUDIO_EXTRACTION(2, "Extracting Audio Stream"),
    ACOUSTIC_ANALYSIS(3, "Acoustic Noise Reduction"),
    SPEECH_TO_TEXT(4, "Neural Speech-to-Text"),
    TIMESTAMP_ALIGNMENT(5, "Word Timestamp Alignment"),
    TOPIC_INDEXING(6, "Semantic Topic Indexing"),
    COMPLETED(7, "Transcript Generated"),
    FAILED(-1, "Transcription Failed")
}

data class TranscriptionJobProgress(
    val jobId: Long,
    val projectId: Long,
    val stage: TranscriptionWorkerStage,
    val progressPercent: Int,
    val statusMessage: String,
    val audioDurationSeconds: Int = 0,
    val wordsTranscribed: Int = 0
)
