# 05. CellTable / Cell Widgets

対象: Cell Tables
- https://www.gwtproject.org/doc/latest/DevGuideUiCellTable.html

## 要点
- 大量データ表示はCellTable系が基本。
- 列定義・ソート・ページャ・行スタイルを分離して設計する。
- `SafeHtml` を使ったセル描画でXSSリスクを抑える。

## QMAClone適用
- `CellTableProblem` は共通テーブルとして複数画面で再利用される。
- 列変更は共通影響（検索画面/Step5/設定）を前提に確認する。
- 行背景やバッジ表現は、可読性と意味の責務分離を優先する。

## 関連
- CSS: [06-css-styling.md](06-css-styling.md)
- SafeHtml: [08-security-safehtml-xsrf.md](08-security-safehtml-xsrf.md)
