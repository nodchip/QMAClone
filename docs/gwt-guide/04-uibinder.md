# 04. UiBinder

対象: UiBinder Guide
- https://www.gwtproject.org/doc/latest/DevGuideUiBinder.html

## 要点
- UIレイアウトをXMLで定義し、Java側でイベント・状態管理を行う。
- `@UiField` による型安全参照と、`@UiHandler` によるイベント接続が基本。
- CSSは `styleName` で接続し、責務分離（構造/見た目/ロジック）を保つ。

## QMAClone適用
- 新規画面はUiBinder優先。
- 既存Java直書きUIは、改修範囲のみ段階移行。
- 文言や導線変更時はUiBinder側配置とJava側表示条件の両方を確認する。

## 典型ミス
- `ui:field` 名不一致
- 非表示制御の初期値とJava制御の二重管理

## 関連
- CSS: [06-css-styling.md](06-css-styling.md)
- CellTable: [05-celltable.md](05-celltable.md)
