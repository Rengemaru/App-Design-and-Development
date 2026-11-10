package com.example.app_design_and_development.quiz

import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.roundToInt

/** 1問分の得点内訳。モック prototype.html の answer() の採点に対応する。 */
data class ScoreBreakdown(
    val base: Int,
    val combo: Int,
    val timeBonus: Int,
) {
    /** この問題で獲得した合計点 */
    val gained: Int get() = base + combo + timeBonus
}

/**
 * 採点・ランク判定の純粋関数群。Android に依存しないため単体テストしやすい。
 * ロジックはモック prototype.html を正とする。
 */
object QuizScoring {

    /**
     * 1問の得点を計算する。
     *
     * @param correct 正解かどうか
     * @param streakAfter この回答を反映した後の連続正解数（正解なら加算済み、不正解なら0）
     * @param remainingSeconds 回答時点の残り秒数（時間切れは0）
     */
    fun scoreAnswer(
        correct: Boolean,
        streakAfter: Int,
        remainingSeconds: Double,
        config: QuizConfig = QuizConfig.DEFAULT,
    ): ScoreBreakdown {
        if (!correct) return ScoreBreakdown(0, 0, 0)
        val combo = max(0, streakAfter - 1) * config.comboStep
        // 時間切れ(0秒)なら +0。正誤・連続正解には干渉しない。
        val timeBonus = (max(0.0, remainingSeconds) * config.timeCoefficient).roundToInt()
        return ScoreBreakdown(config.basePoint, combo, timeBonus)
    }

    /**
     * 全問パーフェクト（全正解・時間ボーナス満タン・連続継続）の理論最大スコア。
     * ランク判定の分母に使う。
     */
    fun theoreticalMax(questionCount: Int, config: QuizConfig = QuizConfig.DEFAULT): Double {
        val maxTimeBonus = config.timeLimitSeconds * config.timeCoefficient
        var sum = 0.0
        for (i in 1..questionCount) {
            sum += config.basePoint + maxTimeBonus + (i - 1) * config.comboStep
        }
        return sum
    }

    /** 獲得スコア比（ratio）から該当ランクを返す。ranks は上から順に判定される。 */
    fun rankFor(ratio: Double, config: QuizConfig = QuizConfig.DEFAULT): Rank =
        config.ranks.first { ratio >= it.min }

    /**
     * 現在のランクから1つ上のランクに到達するのに必要な追加スコア。
     * 最高ランク、またはランク対象外（ranksに無い）の場合は null。
     */
    fun nextRankNeed(
        score: Int,
        rank: Rank,
        theoreticalMax: Double,
        config: QuizConfig = QuizConfig.DEFAULT,
    ): Int? {
        val index = config.ranks.indexOf(rank)
        if (index <= 0) return null
        val up = config.ranks[index - 1]
        val need = ceil(up.min * theoreticalMax).toInt() - score
        return max(1, need)
    }
}
