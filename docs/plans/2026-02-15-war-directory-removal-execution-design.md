# war ディレクトリ削除実行デザイン

## 背景
- 既存計画 `docs/plans/2026-02-15-war-directory-deprecation-plan.md` で、`war/` は非推奨と定義済み。
- 今回は次フェーズとして、`war/` 実体削除を実行するための具体手順を確定する。

## 決定事項
1. `war/` 参照確認は厳格運用とする（`war` 文字列をリポジトリ全体でゼロ化）。
2. `war/` 実体は作業ディレクトリ上で丸ごと削除する（未追跡ファイル含む）。
3. 実行タイミングは即時実行とする。
4. 検証ゲートは `mvn compile` のみとする（デプロイ確認は今回は実施しない）。
5. 失敗時は `git restore --source=HEAD --worktree --staged war` で即時復旧する。

## 実行手順
1. 事前状態を記録する。
   - `git status --short`
   - `rg -n "war/"`
   - `rg -n "\\bwar\\b"`
2. `war/` を丸ごと削除する。
3. 参照ゼロを再確認する。
   - `rg -n "war/"`
   - `rg -n "\\bwar\\b"`
4. ビルド検証を実施する。
   - `mvn compile`
5. 結果を計画書へ反映する。
   - 実行日時
   - 実行コマンド
   - 成否
   - 残課題

## 完了条件
- `war/` 実体が存在しない。
- `war` 参照がリポジトリ全体でゼロである。
- `mvn compile` が成功する。

## 既知の未検証項目
- Tomcat 配備後の HTTP 200/405 は今回未検証。
- 実運用経路のランタイム確認は後続タスクとして扱う。

## ロールバック手順
1. `mvn compile` が失敗、または作業継続が困難な場合は即時復旧する。
2. `git restore --source=HEAD --worktree --staged war` を実行する。
3. `git status --short` で復旧確認後、失敗原因を別タスクで分析する。
