# 06. CSS Styling

対象: CSS Styling
- https://www.gwtproject.org/doc/latest/DevGuideUiCss.html

## 要点
- GWTのスタイル適用は、グローバルCSSとWidget固有スタイルが混在しやすい。
- スコープ衝突を避けるには、用途別クラス命名と責務分離が有効。
- 見た目変更時はホバー/フォーカス/無効状態までセットで調整する。

## QMAClone適用
- `QMAClone.css` は全画面影響のため、変更単位を小さくする。
- CellTable固有見た目は `CellTableProblem.css` と役割分担する。
- 文字コードはUTF-8（BOMなし）を維持する。

## 関連
- UiBinder: [04-uibinder.md](04-uibinder.md)
- CellTable: [05-celltable.md](05-celltable.md)
