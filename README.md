# App-Design-and-Development

## 授業内容 - 背景情報
大学授業 **アプリデザイン発展** の第5〜8回の授業内演習として、Android向けのクイズゲーム「クイズ帳」を作成する。

### 授業回

本開発演習は第5 ~ 8回授業に割り当てられる。

- [x] 第5回: チーム作成 / アプリ内容の構想
- [x] 第6回: 画面UI・クイズカテゴリの決定 / 開発着手
- [ ] 第7回: 引き続き開発を予定
- [ ] 第8回: テスト・クロージングを予定

### チーム体制
4人チームとして、以下の役割分担を行う。

1. リーダー・記録担当
2. 調査・発案担当
3. コーディング主担当 - 処理方針決定・主開発
4. コーディング副担当 - レビュー・テスト

## アプリ内容 - 要件定義

このセクションは主にコーディング担当が担当する。エージェントはコーディング担当を補佐する。

**クイズゲーム**として以下に基づいて作成する。

### 開発環境

- 端末: `Android`
- 言語: `Kotlin`
- プラットフォーム: `Android Studio`

### 処理の流れ (起案)

```mermaid
flowchart LR
    Start([開始]) --> Title[タイトル画面を表示]
    Title -->|スタート| Category[カテゴリ選択画面を表示]
    Category -->|戻る| Title
    Category -->|カテゴリを選択| Init[選択したカテゴリで初期化]

    Init --> Question[問題画面: 問題と選択肢を表示]
    Question -->|選択肢を選ぶ| Judge[答え合わせ・正解数を記録]
    Judge --> Answer[答え画面: 解答結果を表示]
    Answer -->|次へ| Check{n問終了したか}
    Check -->|いいえ| Question
    Check -->|はい| Result[結果画面: 正解数を表示]
    Result -->|タイトルへ| Title
    Result -->|もう一度| Init
```

### 画面設計

| 画面     | 主な要素                            |
| -------- | ----------------------------------- |
| タイトル | タイトル文言 / *スタート*ボタン     |
| カテゴリ | 各*カテゴリ選択*ボタン / 各*戻る*ボタン |
| 問題     | 問題文 / 各*選択肢*ボタン                     |
| こたえ   | 答えの文言 / *次へ*ボタン           |
| 結果     | 結果(正解数, 経過時間, 指標) / *タイトルへ*ボタン / *もう一度*ボタン                                |

画面設計は [docs/wireframe/](docs/wireframe/)、動作モックは [docs/mock/prototype.html](docs/mock/prototype.html) を参照する。
資料の一覧は末尾の「参照」を確認する。

## クイズ内容

このセクションは主にリーダー・記録担当, 調査・発案担当が担当する。エージェントは自走して内容を補完せず、担当者の作成を待つ。

以下に基づいて実装する。

### カテゴリ

- 歴史
- 地理
- 理科・科学
- 英単語
- 難読漢字
- 業界用語
- プログラミング
- アニメ・映画

## 開発の進め方

コーディング主担当が処理方針の決定と主開発を担い、コーディング副担当はレビューとテストを担当する。
副担当とその支援エージェントは、主担当の方針と既存のGit運用に合わせて作業する。
変更は作業ブランチで行い、プルリクエストで共有する。
ブランチ名やPRの書き方は、[PR #1](https://github.com/Rengemaru/App-Design-and-Development/pull/1)（`docs/setup`）を参考にする。

要件と設計の詳細は [docs/](docs/) 配下の資料を正とし、AI支援の進め方は [AGENTS.md](AGENTS.md) に従う。
本READMEの処理フローは起案時の概要であり、採点や制限時間などの詳細は設計資料を参照する。

## 開発の記録 / 実装状況

実装の進捗をチェックボックスで管理する。1機能=1ブランチ、1変更≒1コミット、
完成したらPRを作成する運用で進めている。

### 実装フェーズ

- [x] **ドメイン層**（採点エンジン・モデル） … PR #4 マージ済み
- [x] **データ層**（assets の quiz.json 読み込み） … PR #5 マージ済み
- [ ] **UI層**（各画面とナビゲーション） … ブランチ `feature/quiz-ui` で着手中
- [ ] 画面デザインの適用（モックのビジュアル再現）
- [ ] 仕上げ・動作確認（QAは担当者が実施）

### 完了した機能の内訳

- [x] データモデル `Question` / `Category` / `Rank` / `QuizConfig`（`app/src/main/java/.../quiz/Models.kt`）
- [x] 採点ロジック `QuizScoring`（得点内訳・理論最大スコア・ランク判定・次ランク必要点）
- [x] セッション進行 `QuizEngine`（進行・採点・集計。モックの state/answer/showResult を移植）
- [x] データ読込 `QuizParser` / `QuizRepository`（assets の `quiz.json` をパース）
- [x] 単体テスト 21件（Scoring 8 / Engine 7 / Parser 6）すべてグリーン

### 画面の実装状況（UI層・未着手）

- [ ] タイトル画面（タイトル文言 / スタートボタン）
- [ ] カテゴリ選択画面（カテゴリ一覧 / 戻る）
- [ ] 問題画面（問題文 / 選択肢 / 20秒カウントダウン / 進捗）
- [ ] こたえ画面（正誤 / 答え / 解説 / 連続正解・時間ボーナス / 次へ）
- [ ] 結果画面（ランク / 正解数 / 最大連続 / ボーナス / タイム / もう一度・タイトルへ）
- [ ] 画面遷移の配線（モック同様、単一 Activity での画面切替を想定）

### 仕様・設計の正

- 要件・設計の正は [docs/](docs/) 配下。採点・制限時間・ランクの詳細は
  [docs/mock/prototype.html](docs/mock/prototype.html) の `CONFIG` とロジックを正とする。
- 採点の要点：正解基礎点 100 / 連続正解ボーナス `(連続数-1)×15` /
  時間ボーナス `残り秒×0.5`（満タンで +10）。ランクは `獲得スコア÷理論最大スコア` で
  判定（S:0.92 / A:0.80 / B:0.60 / C:0.40 / D:0、5問未満は対象外）。
- クイズの問題内容は担当者が作成する方針（[AGENTS.md](AGENTS.md)）。現状の
  `app/src/main/assets/quiz.json` はモックのサンプルを移植した差し替え前提のデータ。

### ビルド・テストの注意

- 単体テストの実行：`./gradlew :app:testDebugUnitTest`
- 環境によっては Gradle のツールチェーン自動検出が jlink を持たない JRE を拾い、
  JdkImage 変換で失敗する。その場合は
  `./gradlew :app:testDebugUnitTest -Dorg.gradle.java.installations.auto-detect=false`
  のように自動検出を無効化し、jlink を含む JDK（Android Studio 同梱 JBR など）を使う。
- `org.json` は android.jar ではスタブのため、パーサの単体テスト用に `testImplementation(libs.json)` を追加済み。

### 次の作業（引き継ぎメモ）

- [ ] `feature/quiz-ui` で UI層を実装する（上記「画面の実装状況」を上から順に）。
- [ ] 画面は `QuizRepository` でカテゴリを読み込み、`QuizEngine` で進行・採点する。
- [ ] 完成したら PR を作成してレビューに回す（ブロッキングなPRのみ自己マージ）。

## 参照

以下の資料を参照する。

| 資料 | 配置 |
| --- | --- |
| AI支援の運用方針 | [AGENTS.md](AGENTS.md) |
| 画面構成と遷移 | [docs/wireframe/wireframe.html](docs/wireframe/wireframe.html) |
| デザイン適用後の画面 | [docs/wireframe/screen.html](docs/wireframe/screen.html) |
| 動作モック | [docs/mock/prototype.html](docs/mock/prototype.html) |
| モック用のクイズデータ | [docs/mock/quiz-data.js](docs/mock/quiz-data.js) |
