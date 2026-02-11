# PacketProblemTest Jupiter移行 設計

## 概要
- 対象は `src/test/java/tv/dyndns/kishibe/qmaclone/client/packet/PacketProblemTest.java` のみ。
- 目的は、JUnit4 依存を JUnit5（Jupiter）へ最小差分で移行すること。
- テストロジック・期待値・既存文字列は変更しない。

## 背景
- `client/packet` の Jupiter 移行を段階的に進めている。
- `PacketProblemTest` は件数が多いため、まずフレームワーク依存のみを置換してリスクを抑える。

## 対象範囲
- 変更対象:
  - `src/test/java/tv/dyndns/kishibe/qmaclone/client/packet/PacketProblemTest.java`
- 非対象:
  - 本番コード
  - 他テスト
  - 文字化け文字列やコメント修正（目的外）

## 設計方針
- JUnit4 import / annotation と JUnit4 の `assert*` のみ Jupiter へ置換する。
- Truth / Hamcrest のアサーションは現状維持する。
- `assertThat`（Truth と Hamcrest）の衝突を避けるため import 解決を維持する。

## 変更方針（詳細）
1. import / annotation 置換
- `org.junit.Before` -> `org.junit.jupiter.api.BeforeEach`
- `org.junit.Test` -> `org.junit.jupiter.api.Test`
- `@RunWith(JUnit4.class)` と関連 import を削除

2. JUnit4 assert の置換
- `org.junit.Assert.assertEquals` -> `org.junit.jupiter.api.Assertions.assertEquals`
- `org.junit.Assert.assertTrue` -> `org.junit.jupiter.api.Assertions.assertTrue`
- Hamcrest / Truth の `assertThat` は維持

3. ロジック不変
- 問題文変換、正誤判定、シャッフル、clone/asMinimum、画像URL正規化の検証を維持

## データフローと検証観点
- `setUp()` で `PacketProblem` を生成。
- 各テストで `PacketProblem` の変換・判定・派生データ生成を検証。
- 観点は移行前後で同一。

## リスクと対策
- リスク: `assertThat` import 衝突でコンパイルエラー。
- 対策: 置換範囲を JUnit4 `assert*` に限定し、対象テスト単体実行で確認。
- リスク: 目的外変更混入。
- 対策: 1ファイル限定編集と `git status` で確認。

## 完了条件
- 当該テストが Jupiter でコンパイル・実行できる。
- 既存検証意図が維持される。
- 変更ファイルが対象テスト1ファイルに限定される。

## 検証手順
1. `mvn -DskipTests package`
2. `mvn --% -Dsurefire.skip=false -DfailIfNoTests=false -Dtest=tv.dyndns.kishibe.qmaclone.client.packet.PacketProblemTest test`

## コミット方針
- 1コミット1目的を維持し、設計ドキュメントのみを先にコミットする。