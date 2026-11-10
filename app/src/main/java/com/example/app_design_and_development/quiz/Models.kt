package com.example.app_design_and_development.quiz

/**
 * クイズ1問を表す。モックの quiz-data.js の各問題と対応する。
 *
 * @param text 問題文
 * @param choices 選択肢（通常4つ）
 * @param correctIndex 正解の添字（0始まり）
 * @param explanation 解説文
 * @param hasImage 画像枠を出すかどうか（quiz-data.js の img に対応）
 */
data class Question(
    val text: String,
    val choices: List<String>,
    val correctIndex: Int,
    val explanation: String,
    val hasImage: Boolean = false,
)

/**
 * カテゴリ。1カテゴリ = 1回のクイズセッションの出題範囲。
 *
 * @param name カテゴリ名
 * @param icon カードに出す1文字
 * @param color アイコンの色（#RRGGBB）
 * @param questions 問題の配列
 */
data class Category(
    val name: String,
    val icon: String,
    val color: String,
    val questions: List<Question>,
)

/** ランク1段階。獲得スコア÷理論最大スコアが [min] 以上なら該当する。 */
data class Rank(
    val grade: String,
    val min: Double,
    val color: String,
)

/**
 * 採点・ランク判定の設定値。モック prototype.html の CONFIG を正とする。
 * 調整はここ1か所に集約する。
 */
data class QuizConfig(
    /** 1問あたりの制限時間（秒） */
    val timeLimitSeconds: Int = 20,
    /** 時間ボーナス = 残り秒数 × この係数（満タンで最大 +10pt） */
    val timeCoefficient: Double = 0.5,
    /** 正解の基礎点 */
    val basePoint: Int = 100,
    /** 連続正解ボーナス = (連続数 - 1) × この値 */
    val comboStep: Int = 15,
    /** これ未満の問題数ではランクを出さない */
    val minQuestionsForRank: Int = 5,
    /** 上から順に判定するランク表 */
    val ranks: List<Rank> = DEFAULT_RANKS,
) {
    companion object {
        val DEFAULT_RANKS: List<Rank> = listOf(
            Rank("S", 0.92, "#FFB020"),
            Rank("A", 0.80, "#FF5A5F"),
            Rank("B", 0.60, "#3C8DFF"),
            Rank("C", 0.40, "#2FBF71"),
            Rank("D", 0.00, "#7a84a8"),
        )

        val DEFAULT = QuizConfig()
    }
}
