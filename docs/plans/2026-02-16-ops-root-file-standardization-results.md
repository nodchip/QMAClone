# 運用ファイル標準化 棚卸し結果（ルート直下）

## 実施概要
- 実施日: 2026-02-16
- 対象: リポジトリルート直下ファイル
- 評価軸: 影響度（High/Medium/Low）× 移動容易性（Easy/Medium/Hard）
- 優先度: P1（先行）/ P2（計画後）/ P3（保留）

## 対象一覧と評価
| ファイル | 現状態 | 推奨移動先 | 影響度 | 移動容易性 | 優先度 | 判断理由 |
|---|---|---|---|---|---|---|
| `memo.txt` | 移行済み | `ops/notes/memo.txt` | Low | Easy | P1 | 参照元なし。運用メモとして即時移行可能。 |
| `filter_log.py` | 移行済み | `ops/scripts/filter_log.py` | Medium | Easy | P1 | 参照元なし。単体スクリプトのため移動容易。 |
| `deploy_qmaclone_tomcat9.ps1` | ルート残置 | `ops/scripts/deploy/deploy_qmaclone_tomcat9.ps1` | High | Hard | P2 | AGENTS/ドキュメントで多数参照。参照更新を伴う段階移行が必要。 |

## 対象外（本体ファイル）
以下は運用ファイルではないため、今回の移行対象外。
- `pom.xml`
- `README.md`
- `AGENTS.md`
- `sdk.properties`
- `.gitignore`
- `LICENSE`
- IDE設定ファイル（`.classpath`, `.project`, `.pydevproject`）

## 今回の実施
1. `memo.txt` を `ops/notes/memo.txt` へ移動
2. `filter_log.py` を `ops/scripts/filter_log.py` へ移動
3. `ops/README.md` を現状に合わせて更新

## 次アクション
1. `deploy_qmaclone_tomcat9.ps1` の参照更新計画を作成する
2. 参照更新を伴う移行を1コミット1目的で実施する
3. 移行後に `mvn compile` とデプロイ検証（200/405）を実施する
