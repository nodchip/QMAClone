# 03. コンパイル・デバッグ・CLI

対象:
- Compile & Debug: https://www.gwtproject.org/doc/latest/DevGuideCompilingAndDebugging.html
- Command Line Tools: https://www.gwtproject.org/doc/latest/RefCommandLineTools.html

## 要点
- 主要コマンドは「GWTコンパイル」「テスト」「開発時デバッグ」。
- `localWorkers` で並列コンパイル数を調整可能。
- ドラフトコンパイルは速度優先、最終確認は通常コンパイルで行う。

## QMAClone推奨運用
- 日常: `mvn compile`
- クライアント変更後: `mvn "-Dgwt.skipCompilation=false" gwt:compile`
- 配備: `deploy_qmaclone_tomcat9.ps1`（200/405確認まで）

## 注意点
- `gwt:compile` 失敗時はデプロイを止める。
- コンパイル警告は即座に致命ではないが、同一警告が増える場合は後追いで整理する。

## 関連
- 構成: [02-project-structure.md](02-project-structure.md)
- コード分割: [11-code-splitting.md](11-code-splitting.md)
