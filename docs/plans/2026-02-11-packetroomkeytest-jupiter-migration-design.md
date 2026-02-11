# PacketRoomKeyTest Jupiter移行 設計

## 概要
- 対象は `src/test/java/tv/dyndns/kishibe/qmaclone/client/packet/PacketRoomKeyTest.java` のみ。
- 目的は、JUnit4 依存を JUnit5（Jupiter）へ最小差分で移行すること。
- テストロジックと期待値は変更しない。

## 背景
- `client/packet` の JUnit5 移行を段階的に進める。
- 小粒テストから移行し、失敗時の切り分けを容易にする。

## 対象範囲
- 変更対象:
  - `src/test/java/tv/dyndns/kishibe/qmaclone/client/packet/PacketRoomKeyTest.java`
- 非対象:
  - 本番コード
  - 他テスト
  - `pom.xml` 等のビルド設定

## 設計方針
- JUnit4 import / annotation を Jupiter に置換するのみ。
- `setUp` のテストデータ生成と各アサーションは不変。

## 変更方針（詳細）
1. import / annotation 置換
- `org.junit.Before` -> `org.junit.jupiter.api.BeforeEach`
- `org.junit.Test` -> `org.junit.jupiter.api.Test`
- `org.junit.Assert.assertEquals` -> `org.junit.jupiter.api.Assertions.assertEquals`
- `@RunWith(JUnit4.class)` と関連 import を削除

2. ロジック不変
- `hashCode` / `equals` 検証を維持
- `getGenres` / `getTypes` / `getName` 検証を維持

## データフローと検証観点
- `setUp()` で `EVENT` / `WHOLE` / `THEME` のキーを生成。
- `testHashCode` で同値キー間の hash 一致を検証。
- `testEquals` で同値判定一致を検証。
- 最終テストで constructor / getter 契約を検証。
- 以上の観点は移行前後で同一。

## リスクと対策
- リスク: import 置換漏れによるコンパイルエラー。
- 対策: `build -> test` を直列で実行し確認。

## 完了条件
- 当該テストが Jupiter でコンパイル・実行できる。
- 既存の検証意図（equals/hashCode/getter）が維持される。
- 変更ファイルが対象テスト1ファイルに限定される。

## 検証手順
1. `mvn -DskipTests package`
2. `mvn --% -Dsurefire.skip=false -DfailIfNoTests=false -Dtest=tv.dyndns.kishibe.qmaclone.client.packet.PacketRoomKeyTest test`

## コミット方針
- 1コミット1目的を維持し、設計ドキュメントのみを先にコミットする。