package com.example.app_design_and_development.quiz

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * 採点・ランク判定の純粋関数のテスト。
 * 期待値はモック prototype.html の CONFIG（基礎点100/連続15/時間係数0.5/制限20秒）から算出。
 */
class QuizScoringTest {

    @Test
    fun scoreAnswer_firstCorrectFullTime_hasNoComboAndMaxTimeBonus() {
        val b = QuizScoring.scoreAnswer(correct = true, streakAfter = 1, remainingSeconds = 20.0)
        assertEquals(100, b.base)
        assertEquals(0, b.combo)
        assertEquals(10, b.timeBonus) // 20 * 0.5
        assertEquals(110, b.gained)
    }

    @Test
    fun scoreAnswer_consecutiveCorrect_addsComboByStreak() {
        val b = QuizScoring.scoreAnswer(correct = true, streakAfter = 3, remainingSeconds = 10.0)
        assertEquals(30, b.combo) // (3-1)*15
        assertEquals(5, b.timeBonus) // round(10*0.5)
        assertEquals(135, b.gained)
    }

    @Test
    fun scoreAnswer_wrong_scoresNothing() {
        val b = QuizScoring.scoreAnswer(correct = false, streakAfter = 0, remainingSeconds = 20.0)
        assertEquals(0, b.gained)
    }

    @Test
    fun scoreAnswer_timeoutCorrect_hasNoTimeBonus() {
        val b = QuizScoring.scoreAnswer(correct = true, streakAfter = 1, remainingSeconds = 0.0)
        assertEquals(100, b.base)
        assertEquals(0, b.timeBonus)
    }

    @Test
    fun theoreticalMax_fiveQuestions_is700() {
        assertEquals(700.0, QuizScoring.theoreticalMax(5), 0.0)
    }

    @Test
    fun rankFor_returnsGradeByRatio() {
        assertEquals("S", QuizScoring.rankFor(1.0).grade)
        assertEquals("S", QuizScoring.rankFor(0.92).grade)
        assertEquals("A", QuizScoring.rankFor(0.80).grade)
        assertEquals("B", QuizScoring.rankFor(0.60).grade)
        assertEquals("C", QuizScoring.rankFor(0.40).grade)
        assertEquals("D", QuizScoring.rankFor(0.0).grade)
    }

    @Test
    fun nextRankNeed_belowTop_returnsPointsToNextGrade() {
        // ratio 0.7 -> rank B, next up A (min 0.8)。ceil(0.8*700)=560, 560-490=70
        val theo = 700.0
        val rankB = QuizScoring.rankFor(0.7)
        assertEquals(70, QuizScoring.nextRankNeed(490, rankB, theo))
    }

    @Test
    fun nextRankNeed_topRank_isNull() {
        val rankS = QuizScoring.rankFor(1.0)
        assertNull(QuizScoring.nextRankNeed(700, rankS, 700.0))
    }
}
