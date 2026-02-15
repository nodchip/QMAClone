# 類似問題検索の指摘アイコンモダン化設計

## 目的
- 類似問題検索画面（共通テーブル）の指摘列アイコンをモダン化する。
- 状態意味・判定条件・ツールチップ文言は現行実装と完全互換にする。

## 確定方針
- アイコン形式: PNG から SVG に置換。
- 見た目: 単色アウトライン。
- 状態意味: 現行実装の3状態を維持。
1. 指摘あり（通常警告）
2. 指摘あり（30日超過の強警告）
3. 修正済み確認待ち
- ツールチップ文言: 現行文言を維持。

## 実装範囲
- `src/main/java/tv/dyndns/kishibe/qmaclone/client/report/CellTableProblem.java`
  - 指摘列で参照している画像拡張子を `.png` から `.svg` に変更。
  - 条件分岐ロジックは変更しない。
  - 画像要素にクラスを付与してCSSで見た目を制御する。
- `src/main/webapp/QMAClone.css`
  - 指摘列アイコンのサイズ・配置・輪郭の見え方を調整。
- `src/main/webapp/notification_warning.svg`
- `src/main/webapp/notification_error.svg`
- `src/main/webapp/notification_resolved.svg`

## 実装詳細
- 通常警告: 三角 + 感嘆符（黄系）
- 強警告: 八角 + 感嘆符（赤系）
- 修正済み: 円 + チェック（青系）
- いずれも背景は透明、線主体、16x16基準。

## 検証観点
1. 3状態で表示・色・形が区別できる。
2. 30日超過の状態が通常警告より強く見える。
3. 修正済みが警告と誤認されない。
4. ツールチップ文言が変更されていない。
5. `mvn compile` が成功する。
6. 必要に応じて `mvn test -DfailIfNoTests=false` を実行し結果を記録する。
