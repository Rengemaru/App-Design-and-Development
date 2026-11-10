package com.example.app_design_and_development.quiz

import org.json.JSONArray
import org.json.JSONObject

/**
 * quiz.json の文字列を [Category] のリストへ変換する純粋関数。
 * スキーマは quiz-data.js と同一（categories[].questions[] の q/img/choices/correct/exp）。
 * I/O を持たないため org.json をテスト依存に加えれば単体テストできる。
 */
object QuizParser {

    /** JSON文字列をパースする。不正な構造なら [IllegalArgumentException]。 */
    fun parse(json: String): List<Category> {
        val root = try {
            JSONObject(json)
        } catch (e: Exception) {
            throw IllegalArgumentException("quiz.json の解析に失敗しました", e)
        }
        val catsJson = root.optJSONArray("categories")
            ?: throw IllegalArgumentException("categories が見つかりません")
        return (0 until catsJson.length()).map { i ->
            parseCategory(catsJson.getJSONObject(i), i)
        }
    }

    private fun parseCategory(obj: JSONObject, index: Int): Category {
        val questionsJson = obj.optJSONArray("questions") ?: JSONArray()
        val questions = (0 until questionsJson.length()).map { j ->
            parseQuestion(questionsJson.getJSONObject(j), index, j)
        }
        return Category(
            name = obj.getString("name"),
            icon = obj.optString("icon", ""),
            color = obj.optString("color", "#000000"),
            questions = questions,
        )
    }

    private fun parseQuestion(obj: JSONObject, catIndex: Int, qIndex: Int): Question {
        val choicesJson = obj.getJSONArray("choices")
        val choices = (0 until choicesJson.length()).map { choicesJson.getString(it) }
        val correct = obj.getInt("correct")
        require(correct in choices.indices) {
            "カテゴリ$catIndex 問題$qIndex: correct=$correct が選択肢の範囲外です"
        }
        return Question(
            text = obj.getString("q"),
            choices = choices,
            correctIndex = correct,
            explanation = obj.optString("exp", ""),
            hasImage = obj.optBoolean("img", false),
        )
    }
}
