package com.relaxmind.app.utils

import androidx.compose.ui.graphics.Color
import com.relaxmind.app.ui.themes.ScoreGray
import com.relaxmind.app.ui.themes.ScoreRed
import com.relaxmind.app.ui.themes.ScoreOrange
import com.relaxmind.app.ui.themes.ScoreYellow
import com.relaxmind.app.ui.themes.ScoreGreenLight
import com.relaxmind.app.ui.themes.ScoreGreenDark
import kotlin.math.roundToInt

data class CheckInAnswers(
    val emotionalState: Int,       // 1-5: 1=Muy mal, 5=Excelente
    val sleep: Int?,               // 1-5 (null si es test inicial)
    val energy: Int,               // 1-10
    val stress: Int,               // 1-10
    val frequencyAnswers: List<Int>, // lista de valores 1-5 (Nunca → Siempre)
    val binaryAnswers: List<Int>,   // lista de 0s y 1s (No / Sí)
    val notes: String = ""
)

object WellnessScoreCalculator {

    /**
     * Calculates the wellness score for a single check-in.
     */
    fun calculateScore(answers: CheckInAnswers): Int {
        val b1 = answers.emotionalState.toDouble() / 5.0 * 0.30
        
        val sleepNorm = (answers.sleep ?: 3).toDouble() / 5.0
        val energyNorm = answers.energy.toDouble() / 10.0
        val stressNorm = answers.stress.toDouble() / 10.0
        val b2 = ((sleepNorm + energyNorm + stressNorm) / 3.0) * 0.40
        
        val b3 = if (answers.frequencyAnswers.isEmpty()) 0.0 else {
            answers.frequencyAnswers.average() / 5.0 * 0.20
        }
        
        // Note: binaryAnswers.average() * 0.10 normalizes the Yes/No answers block to a max of 10.0% of the total score.
        // For the documentation example to yield 64/100, the binary answers average must be 0.2 (e.g. 1 "Sí" out of 5 questions).
        val b4 = if (answers.binaryAnswers.isEmpty()) 0.0 else {
            answers.binaryAnswers.average() * 0.10
        }
        
        return ((b1 + b2 + b3 + b4) * 100.0).roundToInt().coerceIn(0, 100)
    }

    /**
     * Calculates the short daily check-in score.
     *
     * Daily check-ins are intentionally brief:
     * emotion, sleep, energy, and stress. Stress is inverted because less stress
     * means better wellbeing.
     */
    fun calculateDailyScore(
        emotionalState: Int,
        sleep: Int,
        energy: Int,
        stress: Int
    ): Int {
        val emotionalBlock = emotionalState.toDouble() / 5.0 * 0.35
        val sleepBlock = sleep.toDouble() / 5.0 * 0.25
        val energyBlock = energy.toDouble() / 10.0 * 0.20
        val stressBlock = ((11 - stress).toDouble() / 10.0) * 0.20

        return ((emotionalBlock + sleepBlock + energyBlock + stressBlock) * 100.0)
            .roundToInt()
            .coerceIn(0, 100)
    }

    /**
     * Calculates the wellness score using Double values, typically used for averages or history calculations.
     */
    fun calculateScore(
        emotionalState: Double,
        sleep: Double?,
        energy: Double,
        stress: Double,
        frequencyAnswers: List<Double>,
        binaryAnswers: List<Double>
    ): Int {
        val b1 = emotionalState / 5.0 * 0.30
        
        val sleepNorm = (sleep ?: 3.0) / 5.0
        val energyNorm = energy / 10.0
        val stressNorm = stress / 10.0
        val b2 = ((sleepNorm + energyNorm + stressNorm) / 3.0) * 0.40
        
        val b3 = if (frequencyAnswers.isEmpty()) 0.0 else {
            frequencyAnswers.average() / 5.0 * 0.20
        }
        
        val b4 = if (binaryAnswers.isEmpty()) 0.0 else {
            binaryAnswers.average() * 0.10
        }
        
        return ((b1 + b2 + b3 + b4) * 100.0).roundToInt().coerceIn(0, 100)
    }

    /**
     * Returns the verbal category based on the wellness score.
     */
    fun getCategory(score: Int): String {
        return when (score) {
            in 0..20 -> "Muy bajo"
            in 21..30 -> "Bajo"
            in 31..60 -> "Moderado"
            in 61..80 -> "Bueno"
            else -> "Excelente"
        }
    }

    fun shouldAlertCaregiver(score: Int): Boolean = score <= 30

    /**
     * Returns the color associated with the wellness score.
     */
    fun getScoreColor(score: Int?): Color {
        return when {
            score == null -> ScoreGray
            score <= 20 -> ScoreRed
            score <= 30 -> ScoreOrange
            score <= 60 -> ScoreYellow
            score <= 80 -> ScoreGreenLight
            else -> ScoreGreenDark
        }
    }
}
