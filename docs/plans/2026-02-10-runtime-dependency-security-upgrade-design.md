# Runtime依存のセキュリティ優先アップグレード設計（最小変更）

## 1. 目的
- 古い runtime 依存関係のセキュリティリスクを下げる。
- 影響範囲を最小化するため、更新対象は合意済みの 5 依存に限定する。
- 更新作業は機能追加と混在させず、依存更新専用で実施する。

## 2. スコープ
### 対象
- `mysql-connector-java`
- `google-http-client`
- `commons-dbcp2`
- `commons-pool2`
- `slf4j-jdk14`

### 非対象
- 上記以外の dependency / plugin の更新
- 仕様変更、機能追加、UI変更
- テスト基盤依存（今回は runtime 優先）

## 3. 方針
- 実行方式は「A: 一括更新 + 段階検証（compile -> test -> package -> deploy）」を採用する。
- 更新先は「メジャーアップ許可」。
- ただし失敗時は依存単位で切り戻しできるよう、変更対象は 5 依存に固定する。

## 4. 実装アーキテクチャ
- `pom.xml` の対象 5 依存のみ version を更新する。
- 変更の追跡を容易にするため、1コミット1目的（依存更新のみ）でまとめる。
- 影響分析は以下の runtime 経路で行う。
  - DB接続系: `commons-dbcp2` + `commons-pool2` + `mysql-connector-java`
  - 外部HTTP通信系: `google-http-client`
  - ログ出力系: `slf4j-jdk14`

## 5. 検証計画
### 機械検証（直列実行）
1. `mvn -DskipTests compile`
2. `mvn -Dtest=PanelSettingUserCodePresenterTest test`
3. `mvn package -DskipTests`
4. `powershell -ExecutionPolicy Bypass -File .\deploy_qmaclone_tomcat9.ps1 -SkipBuild`

### 手動確認
- ログインして設定画面を開けること
- ユーザーコード切替が動作すること
- Google連携表示/解除フローに退行がないこと

## 6. エラーハンドリング
### 失敗分類
1. ビルド失敗（API変更/依存競合）
2. 起動失敗（DBプール/ドライバ/クラスロード）
3. 実行時劣化（通信例外増加/ログ異常/UI退行）

### 切り分け順
- 接続可否 -> 初期化 -> 業務フロー
- DB系失敗時は `mysql-connector-java` と `dbcp2/pool2` を優先調査
- HTTP系失敗時は `google-http-client` を優先調査

### ロールバック
- 依存単位で version を戻す。
- 対象外依存は触らない。

## 7. 完了条件
- 5 依存の更新が `pom.xml` に反映済み
- 機械検証 4 コマンドが成功
- 手動確認 3 項目が成功
- 更新内容/検証結果が記録されている

## 8. 次アクション
- 実装計画（タスク分解）を作成し、更新・検証・切り戻し手順をチケット化する。