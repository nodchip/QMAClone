# JUnit5 併用基盤導入設計

## 1. 目的
- 既存の JUnit4 テスト資産を維持したまま、JUnit5（Jupiter）テストを実行可能にする。
- 依存追加だけでなく、実行基盤（Surefire）を JUnit Platform 対応版へ更新する。

## 2. 方針
- 今回は「併用準備」に限定し、既存テストコードの大規模移行は行わない。
- `junit:junit` は維持し、`junit-jupiter` と `junit-vintage-engine` を `test` で追加する。
- `maven-surefire-plugin` を 3 系へ更新し、JUnit Platform 上で JUnit4/5 を共存実行可能にする。

## 3. 対象・非対象
### 3.1 対象
- `pom.xml` の test 依存追加
1. `org.junit.jupiter:junit-jupiter`
2. `org.junit.vintage:junit-vintage-engine`
- `maven-surefire-plugin` の更新（JUnit Platform 対応）

### 3.2 非対象
- 既存 JUnit4 テストの全面移行
- Failsafe 設定変更
- テストコードの網羅的リファクタリング

## 4. 影響評価
- 主な影響はテスト実行フェーズ（`mvn test`）に限定される。
- `surefire` のバージョン更新によりテスト検出挙動が変わる可能性がある。
- `vintage` を加えることで JUnit4 の互換実行を維持する。

## 5. 検証手順
1. `mvn -DskipTests package`
2. `mvn test`
3. 必要に応じて代表テスト 1 件を明示実行し、JUnit Platform 配下での実効性を確認

## 6. 失敗時対処
- まず Surefire 設定と JUnit Platform 依存解決を確認する。
- 次に `junit-jupiter` と `junit-vintage-engine` のバージョン整合を確認する。
- テストコード修正は最後の手段とし、必要最小限に限定する。

## 7. 完了条件
- `mvn -DskipTests package` が成功する。
- `mvn test` が成功する。
- 差分が `pom.xml` 中心の最小範囲である。
- 1コミット1目的を維持する。
