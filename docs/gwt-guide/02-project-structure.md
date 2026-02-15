# 02. プロジェクト構成（GWTモジュール）

対象: Organize Projects
- https://www.gwtproject.org/doc/latest/DevGuideOrganizingProjects.html

## 要点
- GWTは「モジュール（`.gwt.xml`）」を中心にソース・公開リソース・継承設定を管理する。
- 一般的な配置:
  - client: ブラウザ実行コード
  - shared: client/server共有DTO
  - server: サーバ実行コード
  - public(webapp): 静的リソース
- `inherits` と `source path` の整理が、コンパイル成否と速度に直結する。

## QMAClone適用
- `QMAClone.gwt.xml` の `inherits` 追加/削除は影響が大きいため、変更時は `gwt:compile` を必須実行する。
- CSS/画像追加は `src/main/webapp` とモジュール参照の整合を確認する。

## 関連
- コンパイル: [03-compile-debug-cli.md](03-compile-debug-cli.md)
- UIBinder: [04-uibinder.md](04-uibinder.md)
- 最適化: [11-code-splitting.md](11-code-splitting.md)
