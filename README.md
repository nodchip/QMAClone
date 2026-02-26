# QMAClone

ネットワークリアルタイム対戦型の Web クイズゲームです。  
公開サイト: http://kishibe.dyndns.tv/qmaclone/

## プロジェクト概要

- GWT クライアント + Java サーバーで構成された対戦型クイズアプリケーション
- WAR 形式で Tomcat 10 に配備して動作
- WebSocket と RPC を利用したリアルタイム通信を提供

## 最近の更新（2026-02-26 リリース）

- 画像リンク検査で、同一ホストへのアクセス間隔を制御するレート制限を追加
- 異なるホストへの画像リンク検査を並列化し、全体処理時間の悪化を抑制
- リンク切れ画像検査終了時のスレッド解放を追加し、定期実行の安定性を改善
- ロビーの最近のプレイヤー表示で、6文字のプレイヤー名が不自然に改行される問題を修正

## クイックスタート

前提ツールがインストール済みの状態で、リポジトリルートで以下を実行します。

```powershell
mvn compile
mvn "-Dgwt.skipCompilation=false" gwt:compile
mvn package -DskipTests
.\deploy_qmaclone_tomcat10.ps1
```

デプロイ後の確認:

```powershell
curl.exe -i http://localhost:8080/QMAClone/
curl.exe -i http://localhost:8080/QMAClone/tv.dyndns.kishibe.qmaclone.QMAClone/service
curl.exe -i "http://localhost:8080/QMAClone/tv.dyndns.kishibe.qmaclone.QMAClone/service?warmup=1"
```

## 動作要件

- Windows + PowerShell
- JDK 25（`pom.xml` の `maven.compiler.source/target`）
- Maven
- Tomcat 10.1 系

## 開発手順

### 1. ビルド

```powershell
mvn compile
mvn "-Dgwt.skipCompilation=false" gwt:compile
mvn package -DskipTests
```

### 2. テスト

通常:

```powershell
mvn "-Dsurefire.skip=false" test
```

DB テスト込み:

```powershell
mvn "-Dsurefire.skip=false" -Pwith-db-tests test
```

### 3. デプロイ

```powershell
.\deploy_qmaclone_tomcat10.ps1
```

オプション付き例:

```powershell
.\deploy_qmaclone_tomcat10.ps1 -TomcatBase "C:\ProgramData\Tomcat10" -HostName "localhost"
```

## 設定ファイル

- Tomcat ユーザー設定: `C:\ProgramData\Tomcat10\conf\tomcat-users.xml`
- 管理者プロパティ: `C:\ProgramData\Tomcat10\conf\qmaclone-admin.properties`
- Nginx 設定（運用環境）: `ops/config/live/nginx/sites-enabled/default`

機密情報（パスワード・トークン・鍵など）はリポジトリに平文でコミットしないでください。

## トラブルシュート

- `gwt:compile` が失敗する場合: GWT 非対応 API 使用有無を確認し、修正後に再実行
- デプロイ後に古い画面が表示される場合: `target` / `gwt-unitCache` をクリーンし再ビルド
- WebSocket 接続失敗時: `netstat -> HTTP GET -> Upgrade ハンドシェイク -> サーバーログ` の順で切り分け
- 404 が継続する場合: Tomcat の `webapps/QMAClone` 展開状態と起動ログを確認

## リポジトリ運用

- 運用ファイル配置方針は `ops/README.md` を参照
- 新規の運用ファイルはルート直下ではなく `ops/` 配下へ配置
- 監視プラグイン `qmaclone_` は `ops/scripts/monitoring/qmaclone_` に配置
- 開発運用ルールは `AGENTS.md` を参照

## ライセンス

`LICENSE` を参照してください。
