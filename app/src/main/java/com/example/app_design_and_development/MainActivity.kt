package com.example.app_design_and_development

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.toColorInt
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.app_design_and_development.quiz.AnswerResult
import com.example.app_design_and_development.quiz.Category
import com.example.app_design_and_development.quiz.QuizEngine
import com.example.app_design_and_development.quiz.QuizRepository
import kotlin.math.ceil
import kotlin.math.roundToInt

/**
 * 単一 Activity でタイトル→カテゴリ→問題→こたえ→結果の5画面を切り替える。
 * 進行・採点は [QuizEngine]、データ読込は [QuizRepository] に委譲し、
 * この画面は表示とタイマー、入力のハンドリングに専念する。
 * 画面構成・配色・採点はモック prototype.html を正とする。
 */
class MainActivity : AppCompatActivity() {

    private enum class Screen { TITLE, CATEGORY, QUESTION, ANSWER, RESULT }

    private lateinit var screens: Map<Screen, View>

    private var categories: List<Category> = emptyList()
    private var engine: QuizEngine? = null
    private var currentCategory: Category? = null

    // タイマー関連
    private val handler = Handler(Looper.getMainLooper())
    private var timerEndAt: Long = 0L
    private var remainingSeconds: Double = 0.0
    private val timerTick = object : Runnable {
        override fun run() {
            remainingSeconds = ((timerEndAt - System.currentTimeMillis()) / 1000.0).coerceAtLeast(0.0)
            renderTimer()
            if (remainingSeconds > 0.0) {
                handler.postDelayed(this, TIMER_INTERVAL_MS)
            }
        }
    }

    private val timeLimit: Int get() = engine?.config?.timeLimitSeconds ?: 20

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        val root = findViewById<View>(R.id.main)
        root.background = NotebookDrawable(resources.displayMetrics.density)
        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        screens = mapOf(
            Screen.TITLE to findViewById(R.id.screen_title),
            Screen.CATEGORY to findViewById(R.id.screen_category),
            Screen.QUESTION to findViewById(R.id.screen_question),
            Screen.ANSWER to findViewById(R.id.screen_answer),
            Screen.RESULT to findViewById(R.id.screen_result),
        )

        categories = try {
            QuizRepository(this).loadCategories()
        } catch (_: Exception) {
            emptyList()
        }

        findViewById<View>(R.id.btn_start).setOnClickListener { showCategory() }
        findViewById<View>(R.id.btn_back_category).setOnClickListener { show(Screen.TITLE) }
        findViewById<View>(R.id.btn_quit_question).setOnClickListener { confirmQuit() }
        findViewById<View>(R.id.btn_quit_answer).setOnClickListener { confirmQuit() }
        findViewById<View>(R.id.btn_to_title).setOnClickListener { show(Screen.TITLE) }

        show(Screen.TITLE)
    }

    override fun onDestroy() {
        stopTimer()
        super.onDestroy()
    }

    // ---- 画面遷移 -------------------------------------------------------

    private fun show(screen: Screen) {
        if (screen == Screen.TITLE || screen == Screen.CATEGORY) stopTimer()
        screens.forEach { (key, view) ->
            view.visibility = if (key == screen) View.VISIBLE else View.GONE
        }
    }

    private fun showCategory() {
        val list = findViewById<LinearLayout>(R.id.category_list)
        list.removeAllViews()
        if (categories.isEmpty()) {
            val error = TextView(this).apply {
                text = getString(R.string.load_error)
                setTextColor(getColor(R.color.ink))
            }
            list.addView(error)
        } else {
            categories.forEachIndexed { index, category -> list.addView(buildCategoryItem(category, index)) }
        }
        show(Screen.CATEGORY)
    }

    private fun buildCategoryItem(category: Category, index: Int): View {
        val item = layoutInflater.inflate(R.layout.item_category, findViewById(R.id.category_list), false)
        item.findViewById<TextView>(R.id.category_icon).apply {
            text = category.icon
            backgroundTintList = colorOf(category.color)
        }
        item.findViewById<TextView>(R.id.category_name).text = category.name
        item.findViewById<TextView>(R.id.category_count).text =
            getString(R.string.category_count, category.questions.size)
        item.setOnClickListener { startQuiz(index) }
        return item
    }

    // ---- クイズ進行 -----------------------------------------------------

    private fun startQuiz(categoryIndex: Int) {
        val category = categories[categoryIndex]
        currentCategory = category
        engine = QuizEngine(category.questions)
        show(Screen.QUESTION)
        loadQuestion()
    }

    private fun loadQuestion() {
        val engine = engine ?: return
        val category = currentCategory ?: return
        val question = engine.currentQuestion
        val number = getString(R.string.q_progress, engine.index + 1, engine.total)

        findViewById<TextView>(R.id.question_number).text = number
        findViewById<TextView>(R.id.answer_number).text = number
        findViewById<TextView>(R.id.question_category).text = category.name
        findViewById<TextView>(R.id.question_score).text = getString(R.string.score_pt, engine.score)
        findViewById<TextView>(R.id.answer_score).text = getString(R.string.score_pt, engine.score)
        findViewById<TextView>(R.id.question_streak).text =
            if (engine.streak >= 2) getString(R.string.streak_ongoing, engine.streak) else ""
        findViewById<ProgressBar>(R.id.question_progress).progress =
            (engine.index.toDouble() / engine.total * 100).roundToInt()
        findViewById<TextView>(R.id.question_text).text = question.text
        findViewById<View>(R.id.question_image).visibility =
            if (question.hasImage) View.VISIBLE else View.GONE

        buildChoices(question.choices)
        startTimer()
    }

    private fun buildChoices(choices: List<String>) {
        val container = findViewById<LinearLayout>(R.id.choice_list)
        container.removeAllViews()
        choices.forEachIndexed { index, text ->
            val item = layoutInflater.inflate(R.layout.item_choice, container, false)
            item.findViewById<TextView>(R.id.choice_label).apply {
                this.text = LABELS.getOrElse(index) { "?" }
                backgroundTintList = colorOf(CHOICE_COLORS[index % CHOICE_COLORS.size])
            }
            item.findViewById<TextView>(R.id.choice_text).text = text
            item.setOnClickListener { answer(index) }
            container.addView(item)
        }
    }

    private fun answer(choiceIndex: Int) {
        val engine = engine ?: return
        stopTimer()
        val result = engine.answer(choiceIndex, remainingSeconds)
        renderAnswer(result)
        show(Screen.ANSWER)
    }

    private fun renderAnswer(result: AnswerResult) {
        val engine = engine ?: return
        val question = engine.currentQuestion

        findViewById<TextView>(R.id.answer_score).text = getString(R.string.score_pt, engine.score)

        val mark = findViewById<TextView>(R.id.verdict_mark)
        val verdict = findViewById<TextView>(R.id.verdict_text)
        if (result.correct) {
            mark.setBackgroundResource(R.drawable.bg_verdict_correct)
            mark.text = getString(R.string.mark_correct)
            mark.setTextColor(getColor(R.color.primary))
            verdict.text = getString(R.string.verdict_correct)
            verdict.setTextColor(getColor(R.color.primary))
        } else {
            mark.setBackgroundResource(R.drawable.bg_verdict_wrong)
            mark.text = getString(R.string.mark_wrong)
            mark.setTextColor(getColor(R.color.wrong_line))
            verdict.text = getString(R.string.verdict_wrong)
            verdict.setTextColor(getColor(R.color.wrong_line))
        }

        findViewById<TextView>(R.id.answer_correct).text = question.choices[result.correctIndex]
        findViewById<TextView>(R.id.answer_your).apply {
            if (result.correct) {
                visibility = View.GONE
            } else {
                visibility = View.VISIBLE
                text = getString(R.string.your_answer, question.choices[result.selectedIndex])
            }
        }
        findViewById<TextView>(R.id.answer_explanation).text = question.explanation

        val combo = findViewById<TextView>(R.id.combo_bonus)
        when {
            result.correct && result.breakdown.combo > 0 -> {
                combo.visibility = View.VISIBLE
                combo.text = getString(R.string.combo_bonus, result.streak, result.breakdown.combo)
            }
            !result.correct -> {
                combo.visibility = View.VISIBLE
                combo.text = getString(R.string.combo_reset)
            }
            else -> combo.visibility = View.GONE
        }

        val time = findViewById<TextView>(R.id.time_bonus)
        if (result.correct) {
            time.visibility = View.VISIBLE
            time.text = if (result.breakdown.timeBonus > 0) {
                getString(R.string.time_bonus, remainingSeconds.roundToInt(), result.breakdown.timeBonus)
            } else {
                getString(R.string.time_bonus_none)
            }
        } else {
            time.visibility = View.GONE
        }

        findViewById<TextView>(R.id.btn_next).apply {
            text = getString(if (result.isLastQuestion) R.string.see_result else R.string.next_question)
            setOnClickListener {
                if (result.isLastQuestion) showResult() else nextQuestion()
            }
        }
    }

    private fun nextQuestion() {
        engine?.next()
        show(Screen.QUESTION)
        loadQuestion()
    }

    private fun showResult() {
        val engine = engine ?: return
        val category = currentCategory ?: return
        val result = engine.result()

        findViewById<TextView>(R.id.result_category).text = category.name

        val big = findViewById<View>(R.id.result_big)
        val rankLabel = findViewById<TextView>(R.id.result_rank_label)
        val rank = findViewById<TextView>(R.id.result_rank)
        val correct = findViewById<TextView>(R.id.result_correct)
        val message = findViewById<TextView>(R.id.result_message)

        if (result.rank != null) {
            val highlight = result.ratio >= 0.6
            big.backgroundTintList =
                colorOf(if (highlight) "#FFE066" else "#E3E7F3")
            rankLabel.visibility = View.VISIBLE
            rank.text = result.rank.grade
            rank.setTextColor(result.rank.color.toColorInt())
            correct.text = getString(R.string.result_correct, result.correctCount, result.total)
            message.text = if (result.nextRankNeed != null) {
                val next = engine.config.ranks[engine.config.ranks.indexOf(result.rank) - 1]
                getString(R.string.next_rank, result.nextRankNeed, next.grade)
            } else {
                getString(R.string.top_rank)
            }
            message.setTextColor(getColor(if (highlight) R.color.primary else R.color.mute))
        } else {
            big.backgroundTintList = colorOf("#FFE066")
            rankLabel.visibility = View.GONE
            rank.text = result.correctCount.toString()
            rank.setTextColor(getColor(R.color.primary))
            correct.text = getString(R.string.result_correct, result.correctCount, result.total)
            message.text = getString(R.string.rank_min_note)
            message.setTextColor(getColor(R.color.mute))
        }

        findViewById<TextView>(R.id.stat_streak).text =
            getString(R.string.streak_count, result.maxStreak)
        findViewById<TextView>(R.id.stat_bonus).text =
            getString(R.string.bonus_total, result.bonusTotal)
        findViewById<TextView>(R.id.stat_time).text = formatTime(result.elapsedSeconds)
        findViewById<TextView>(R.id.result_total).text = getString(R.string.score_pt, result.score)

        findViewById<TextView>(R.id.btn_retry).setOnClickListener {
            startQuiz(categories.indexOf(category))
        }

        show(Screen.RESULT)
    }

    // ---- タイマー -------------------------------------------------------

    private fun startTimer() {
        stopTimer()
        remainingSeconds = timeLimit.toDouble()
        timerEndAt = System.currentTimeMillis() + timeLimit * 1000L
        handler.post(timerTick)
    }

    private fun stopTimer() {
        handler.removeCallbacks(timerTick)
    }

    private fun renderTimer() {
        findViewById<TextView>(R.id.timer_text).text =
            getString(R.string.time_seconds, ceil(remainingSeconds).toInt())
        val ratio = (remainingSeconds / timeLimit).coerceIn(0.0, 1.0)
        val bar = findViewById<ProgressBar>(R.id.timer_bar)
        bar.progress = (ratio * bar.max).roundToInt()
        val colorRes = when {
            ratio <= 0.25 -> R.color.timer_danger
            ratio <= 0.5 -> R.color.timer_warn
            else -> R.color.timer_ok
        }
        bar.progressTintList = android.content.res.ColorStateList.valueOf(getColor(colorRes))
    }

    // ---- その他 ---------------------------------------------------------

    private fun confirmQuit() {
        AlertDialog.Builder(this)
            .setMessage(R.string.quit_message)
            .setPositiveButton(R.string.quit_ok) { _, _ -> show(Screen.TITLE) }
            .setNegativeButton(R.string.quit_cancel, null)
            .show()
    }

    private fun colorOf(hex: String) =
        android.content.res.ColorStateList.valueOf(hex.toColorInt())

    private fun formatTime(seconds: Double): String {
        val total = seconds.roundToInt()
        return "%d:%02d".format(total / 60, total % 60)
    }

    companion object {
        private const val TIMER_INTERVAL_MS = 50L
        private val LABELS = listOf("A", "B", "C", "D")
        private val CHOICE_COLORS = listOf("#FF5A5F", "#3C8DFF", "#2FBF71", "#FFB020")
    }
}
