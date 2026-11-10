package com.example.app_design_and_development.quiz

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/** QuizParser のテスト。org.json をテスト依存に加えて実装を利用する。 */
class QuizParserTest {

    @Test
    fun parse_readsCategoriesAndQuestions() {
        val json = """
            {"categories":[
              {"name":"地理","icon":"A","color":"#FF0000","questions":[
                {"q":"首都は？","img":true,"choices":["東京","大阪"],"correct":0,"exp":"東京です"}
              ]}
            ]}
        """.trimIndent()
        val cats = QuizParser.parse(json)
        assertEquals(1, cats.size)
        val c = cats[0]
        assertEquals("地理", c.name)
        assertEquals("A", c.icon)
        assertEquals("#FF0000", c.color)
        assertEquals(1, c.questions.size)
        val q = c.questions[0]
        assertEquals("首都は？", q.text)
        assertEquals(listOf("東京", "大阪"), q.choices)
        assertEquals(0, q.correctIndex)
        assertEquals("東京です", q.explanation)
        assertTrue(q.hasImage)
    }

    @Test
    fun parse_imgDefaultsToFalse() {
        val json = """{"categories":[{"name":"n","icon":"A","color":"#000","questions":[
            {"q":"q","choices":["a","b"],"correct":1,"exp":"e"}]}]}"""
        assertEquals(false, QuizParser.parse(json)[0].questions[0].hasImage)
    }

    @Test(expected = IllegalArgumentException::class)
    fun parse_invalidJson_throws() {
        QuizParser.parse("not json")
    }

    @Test(expected = IllegalArgumentException::class)
    fun parse_missingCategories_throws() {
        QuizParser.parse("""{"foo":1}""")
    }

    @Test(expected = IllegalArgumentException::class)
    fun parse_correctOutOfRange_throws() {
        val json = """{"categories":[{"name":"n","icon":"A","color":"#000","questions":[
            {"q":"q","choices":["a","b"],"correct":5,"exp":"e"}]}]}"""
        QuizParser.parse(json)
    }

    /** 実際に同梱する assets/quiz.json が壊れていないことを保証する。 */
    @Test
    fun bundledQuizJson_isValid() {
        val file = File("src/main/assets/quiz.json")
        assertTrue("quiz.json が見つからない: ${file.absolutePath}", file.exists())
        val cats = QuizParser.parse(file.readText())
        assertTrue("カテゴリが空", cats.isNotEmpty())
        cats.forEach { c ->
            assertTrue("${c.name} の問題が空", c.questions.isNotEmpty())
            c.questions.forEach { q ->
                assertTrue(q.correctIndex in q.choices.indices)
            }
        }
    }
}
