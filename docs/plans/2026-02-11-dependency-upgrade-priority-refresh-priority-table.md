# 依存関係更新 優先度表（2026-02-11）

## 評価基準

- 優先順: `Security -> Compatibility -> Effect`
- 対象: 本番依存 / テスト依存 / ビルド依存
- 判定: 上位3件を先行実装（直列）

## 更新候補サマリ（抜粋）

| Target | Current -> Target | Security | Compatibility | Effect | Priority |
| --- | --- | --- | --- | --- | --- |
| `commons-fileupload:commons-fileupload` | `1.5 -> 1.6.0` | 高 | 中 | 中 | 1 |
| `commons-io:commons-io` | `2.4 -> 2.21.0` | 高 | 中 | 中 | 1a |
| `com.google.inject:guice` + extensions | `4.2.3 -> 7.0.0` | 中 | 高 | 高 | 2 |
| `net.java.dev.jna:jna` + `jna-platform` | `5.5.0 -> 5.18.1` | 中 | 中 | 高 | 3 |
| `org.apache.lucene:*` | `5.5.5 -> 10.x` | 中 | 高 | 高 | 保留 |
| `org.eclipse.jetty:*` | `9.x -> 11/12` | 中 | 高 | 高 | 保留 |
| `org.junit.jupiter:junit-jupiter` | `5.10.2 -> 6.1.0-M1` | 低 | 高 | 中 | 保留 |

## 上位3件の確定

1. `commons-fileupload` / `commons-io` 更新
- 理由: セキュリティ優先軸で最上位。サーブレットアップロード処理に直結。

2. `com.google.inject:*` 更新（Guice 7）
- 理由: Java 25 / JUnit5 移行と衝突しやすい依存の中心。互換性評価が高い。

3. `net.java.dev.jna:*` 更新
- 理由: ネイティブ連携（`zinnia.dll`）の実行安定性と保守性への効果が高い。

## 実行順序

1. `commons-fileupload` + `commons-io`
2. `guice` + `guice-assistedinject` + `guice-multibindings`
3. `jna` + `jna-platform`

上記を 1件ずつ `build -> test` 成功後に次へ進める。
