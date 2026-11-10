package com.example.app_design_and_development.quiz

import android.content.Context

/**
 * assets から quiz.json を読み込み、カテゴリ一覧を提供する。
 * パース処理は [QuizParser] に委譲する（こちらは I/O のみ）。
 */
class QuizRepository(
    private val context: Context,
    private val assetName: String = DEFAULT_ASSET,
) {
    /** 読み込み済みカテゴリを返す。ファイルが無い・壊れている場合は例外。 */
    fun loadCategories(): List<Category> {
        val json = context.assets.open(assetName).bufferedReader().use { it.readText() }
        return QuizParser.parse(json)
    }

    companion object {
        const val DEFAULT_ASSET = "quiz.json"
    }
}
