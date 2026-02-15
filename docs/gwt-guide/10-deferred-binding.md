# 10. Deferred Binding

対象: Deferred Binding
- https://www.gwtproject.org/doc/latest/DevGuideCodingBasicsDeferred.html

## 要点
- GWTのDeferred Bindingは、コンパイル時に実装を切り替える仕組み。
- `GWT.create()` と `.gwt.xml` の設定が連携して動作する。
- 過剰に使うと可読性が落ちるため、用途を限定する。

## QMAClone適用
- 既存の `GWT.create()` 利用箇所を変更する際は、モジュール設定まで確認する。
- 依存ライブラリ更新時はrebindエラー発生有無を重点確認する。

## 関連
- プロジェクト構成: [02-project-structure.md](02-project-structure.md)
- コンパイル: [03-compile-debug-cli.md](03-compile-debug-cli.md)
