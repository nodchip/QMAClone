# 11. Code Splitting

対象: Code Splitting
- https://www.gwtproject.org/doc/latest/DevGuideCodeSplitting.html

## 要点
- 初期ロードを軽くするため、機能単位でコードを分割できる。
- 分割ポイントの設計次第で、初回体験と操作遅延が変わる。

## QMAClone適用
- 常時使わない重い画面（統計、設定、管理機能）は分割候補。
- ただし分割しすぎると体感遅延が増えるため、画面遷移頻度で判断する。

## 関連
- コンパイル: [03-compile-debug-cli.md](03-compile-debug-cli.md)
- ClientBundle: [12-clientbundle.md](12-clientbundle.md)
