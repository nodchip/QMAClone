# ops ディレクトリ運用

このディレクトリは、QMAClone の運用ファイル置き場です。
ソースコードや設計文書とは分離し、作業ログ・メモ・運用スクリプトを集約します。

## 構成
- `ops/log/`: ローカル検証ログや一時ログ
- `ops/notes/`: 作業メモや運用メモ
- `ops/scripts/`: 運用補助スクリプト
- `ops/scripts/monitoring/`: 監視プラグイン（例: Munin）
- `ops/config/`: ローカル運用設定ファイル置き場（`live` は Git 除外）

## Git 方針
- `ops/log/` と `ops/notes/` の実データは `.gitignore` で除外します。
- `ops/config/live/` の実運用設定ファイルは `.gitignore` で除外します。
- 追跡対象はテンプレート（`*.example`）と説明ファイルのみです。
- `ops/scripts/` は再現性のため原則として追跡対象です。

## 段階移行
- ルート直下の `memo.txt` は `ops/notes/memo.txt` へ移行済みです。
- ルート直下の `filter_log.py` は `ops/scripts/filter_log.py` へ移行済みです。
- `qmaclone_`（Muninプラグイン）は `ops/scripts/monitoring/qmaclone_` へ移動済みです。
- 今後の新規/更新分は、必ず `ops/` 配下へ配置します。

## レガシー運用ファイル
- ルート直下の標準運用スクリプトは `deploy_qmaclone_tomcat10.ps1` です。
- `deploy_qmaclone_tomcat9.ps1` は互換ラッパーとして残置しています。
