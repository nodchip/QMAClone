# 管理者アクセス制御 設計（2026-02-20）

## 目的
- `#administratormode` 依存を廃止する。
- 管理画面の表示条件を以下に統一する。
  - Google連携情報あり（`authProvider=google` かつ `authSubject` 非空）
  - `authSubject` が allowlist に存在
  - `lookupUserDataByExternalAccount(provider, subject)` に現在ユーザーコードが含まれる
- 管理画面から使用される管理系RPCのみをサーバ側で拒否可能にする。

## 対象RPC（管理画面由来のみ）
- `getThemeModeEditors`
- `applyThemeModeEditor`
- `acceptThemeModeEditor`
- `rejectThemeModeEditor`
- `getRestrictedUserCodes`
- `addRestrictedUserCode`
- `removeRestrictedUserCode`
- `clearRestrictedUserCodes`
- `getRestrictedRemoteAddresses`
- `addRestrictedRemoteAddress`
- `removeRestrictedRemoteAddress`
- `clearRestrictedRemoteAddresses`

## 設定ファイル
- 配置: `ops/config/live/tomcat9/qmaclone-admin.properties`（Git管理外）
- 読み込み優先順:
  1. `-Dqmaclone.admin.config=<path>`
  2. 既定 `ops/config/live/tomcat9/qmaclone-admin.properties`
- キー:
  - `admin.enforcement.enabled=true|false`
  - `admin.google.sub.allowlist=sub1,sub2,...`

## 実装方針
- サーバに `AdminAccessManager` を追加し、設定読み込みと判定を集約する。
- `login` 応答に `administratorMode` を追加し、クライアントが `SharedData` に反映する。
- `PanelSetting` の「管理者用」表示は `SharedData.isAdministoratorMode()` のみで制御する。
- `QMAClone` の `#administratormode` 判定を削除する。
- 12件の管理RPC先頭で `requireAdministrator()` を実行して拒否する。
- `ThemeModeEditorManager` の通知URLから `#administratormode` を削除する。

## エラー方針
- 設定ファイル未配置、読み込み失敗、allowlist未一致、連携不整合はすべて非管理者扱い。
- 管理RPCで未認可の場合は `ServiceException("管理者権限が必要です")` を返す。

## 検証
- 単体テスト: `ServiceServletStubTest` で管理RPCの許可/拒否を確認。
- 回帰テスト: `mvn "-Dsurefire.skip=false" test`
- 配備後疎通:
  - `/QMAClone-1.0-SNAPSHOT/` -> `HTTP 200`
  - `/tv.dyndns.kishibe.qmaclone.QMAClone/service` -> `HTTP 405`
  - `/tv.dyndns.kishibe.qmaclone.QMAClone/service?warmup=1` -> `HTTP 200`
