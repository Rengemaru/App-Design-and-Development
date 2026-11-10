package com.example.app_design_and_development.quiz

/** 1問回答した結果。こたえ画面の表示に必要な情報をまとめる。 */
data class AnswerResult(
    val correct: Boolean,
    /** 正解の選択肢の添字 */
    val correctIndex: Int,
    /** ユーザーが選んだ選択肢の添字 */
    val selectedIndex: Int,
    val breakdown: ScoreBreakdown,
    /** 回答後の連続正解数（不正解なら0） */
    val streak: Int,
    /** 回答後の累計スコア */
    val totalScore: Int,
    /** 最後の問題だったか */
    val isLastQuestion: Boolean,
)

/** 結果画面に表示する最終集計。 */
data class QuizResult(
    val correctCount: Int,
    val total: Int,
    val score: Int,
    val maxStreak: Int,
    val bonusTotal: Int,
    val elapsedSeconds: Double,
    /** ランク対象外（問題数が minQuestionsForRank 未満）なら null */
    val rank: Rank?,
    val ratio: Double,
    /** 次ランクまでの必要点。最高ランク・ランク対象外なら null */
    val nextRankNeed: Int?,
)

/**
 * 1回のクイズセッションの進行と採点を担う。
 * モック prototype.html の state / answer / showResult のロジックを移植する。
 * UI・タイマー・Androidには依存しない。
 */
class QuizEngine(
    private val questions: List<Question>,
    val config: QuizConfig = QuizConfig.DEFAULT,
) {
    init {
        require(questions.isNotEmpty()) { "questions must not be empty" }
    }

    val total: Int = questions.size

    var index: Int = 0
        private set
    var score: Int = 0
        private set
    var correctCount: Int = 0
        private set
    var streak: Int = 0
        private set
    var maxStreak: Int = 0
        private set
    var bonusTotal: Int = 0
        private set
    var elapsedSeconds: Double = 0.0
        private set

    private var answered = false

    val currentQuestion: Question get() = questions[index]
    val isLastQuestion: Boolean get() = index >= total - 1

    /**
     * 現在の問題に回答する。
     *
     * @param choiceIndex 選んだ選択肢の添字
     * @param remainingSeconds 回答時点の残り秒数（時間切れは0）
     */
    fun answer(choiceIndex: Int, remainingSeconds: Double): AnswerResult {
        check(!answered) { "current question is already answered" }
        answered = true

        val q = currentQuestion
        elapsedSeconds += (config.timeLimitSeconds - remainingSeconds)
        val correct = choiceIndex == q.correctIndex

        if (correct) {
            correctCount++
            streak++
            if (streak > maxStreak) maxStreak = streak
        } else {
            streak = 0
        }

        val breakdown = QuizScoring.scoreAnswer(correct, streak, remainingSeconds, config)
        score += breakdown.gained
        bonusTotal += breakdown.combo + breakdown.timeBonus

        return AnswerResult(
            correct = correct,
            correctIndex = q.correctIndex,
            selectedIndex = choiceIndex,
            breakdown = breakdown,
            streak = streak,
            totalScore = score,
            isLastQuestion = isLastQuestion,
        )
    }

    /** 次の問題へ進む。最後の問題の後に呼ぶと例外。 */
    fun next() {
        check(answered) { "answer the current question before advancing" }
        check(!isLastQuestion) { "no more questions" }
        index++
        answered = false
    }

    /** 最終結果を集計する。全問回答後に呼ぶ想定。 */
    fun result(): QuizResult {
        val theo = QuizScoring.theoreticalMax(total, config)
        val ratio = if (theo > 0) score / theo else 0.0
        val showRank = total >= config.minQuestionsForRank
        val rank = if (showRank) QuizScoring.rankFor(ratio, config) else null
        val nextNeed = rank?.let { QuizScoring.nextRankNeed(score, it, theo, config) }
        return QuizResult(
            correctCount = correctCount,
            total = total,
            score = score,
            maxStreak = maxStreak,
            bonusTotal = bonusTotal,
            elapsedSeconds = elapsedSeconds,
            rank = rank,
            ratio = ratio,
            nextRankNeed = nextNeed,
        )
    }
}
