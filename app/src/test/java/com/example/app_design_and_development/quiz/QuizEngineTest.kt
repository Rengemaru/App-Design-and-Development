package com.example.app_design_and_development.quiz

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** QuizEngine の進行・採点・集計のテスト。 */
class QuizEngineTest {

    private fun q(correct: Int) = Question(
        text = "q",
        choices = listOf("a", "b", "c", "d"),
        correctIndex = correct,
        explanation = "exp",
    )

    private fun engine(n: Int) = QuizEngine(List(n) { q(0) })

    @Test
    fun perfectRun_fiveQuestions_scores700AndRankS() {
        val e = engine(5)
        repeat(5) {
            e.answer(choiceIndex = 0, remainingSeconds = 20.0)
            if (!e.isLastQuestion) e.next()
        }
        val r = e.result()
        assertEquals(700, r.score)
        assertEquals(5, r.correctCount)
        assertEquals(5, r.maxStreak)
        assertEquals("S", r.rank?.grade)
        assertNull(r.nextRankNeed) // 最高ランク
    }

    @Test
    fun wrongAnswer_resetsStreakButKeepsMaxStreak() {
        val e = engine(5)
        e.answer(0, 20.0); e.next() // 正解 streak1
        e.answer(0, 20.0); e.next() // 正解 streak2
        val wrong = e.answer(1, 20.0)
        assertFalse(wrong.correct)
        assertEquals(0, wrong.streak)
        e.next()
        e.answer(0, 20.0); e.next()
        e.answer(0, 20.0)
        val r = e.result()
        assertEquals(4, r.correctCount)
        assertEquals(2, r.maxStreak)
    }

    @Test
    fun elapsedSeconds_accumulatesUsedTime() {
        val e = engine(2)
        e.answer(0, remainingSeconds = 15.0); e.next() // 使用5秒
        e.answer(0, remainingSeconds = 5.0) // 使用15秒
        assertEquals(20.0, e.result().elapsedSeconds, 0.0001)
    }

    @Test
    fun fewerThanMinQuestions_hasNoRank() {
        val e = engine(3)
        repeat(3) {
            e.answer(0, 20.0)
            if (!e.isLastQuestion) e.next()
        }
        val r = e.result()
        assertNull(r.rank)
        assertNull(r.nextRankNeed)
        assertEquals(3, r.correctCount)
    }

    @Test
    fun isLastQuestion_trueOnFinalIndex() {
        val e = engine(2)
        assertFalse(e.isLastQuestion)
        e.answer(0, 20.0); e.next()
        assertTrue(e.isLastQuestion)
    }

    @Test(expected = IllegalStateException::class)
    fun answeringTwice_throws() {
        val e = engine(2)
        e.answer(0, 20.0)
        e.answer(0, 20.0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun emptyQuestions_rejected() {
        QuizEngine(emptyList())
    }
}
