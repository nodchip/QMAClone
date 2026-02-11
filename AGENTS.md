# AGENTS.md（QMAClone）

## 適用範囲
- このファイルは QMAClone 固有ルールのみを扱う。
- 共通ルールは `C:\Users\nodchip\.codex\AGENTS.md` を参照する。

## 目的
- QMAClone で再発した障害を、環境差分（DevMode / Jetty / Tomcat / GWT / WebSocket）を前提に再発防止する。

## 過去の作業ミス（QMAClone固有）と改善案
- ミス: DevMode と Tomcat の差を考慮せず、クラスローダ起因の `NoClassDefFoundError` / `ClassCastException` を見落とした。
- 改善: Jetty 関連変更時は両例外をセットで確認し、DevMode と Tomcat で個別に起動検証する。
- ミス: GWT 再コンパイル失敗後に古い `cache.js` が配信され、画面不整合が起きた。
- 改善: クライアント変更の完了条件に「GWT 再コンパイル成功」と「配備先で最新成果物が参照されること」を含める。
- ミス: WebSocket URL を環境固定値で扱い、接続先不一致（404/500）を誘発した。
- 改善: URL は `protocol / host / contextPath` から導出し、クライアントログに接続先 URL を必ず含める。
- ミス: WebSocket 障害をサーバーログ確認前に断定し、調査が遠回りになった。
- 改善: `netstat -> HTTP GET -> Upgrade ハンドシェイク -> サーバーログ` の順で切り分ける。
- ミス: Tomcat 再配備時に旧状態が残り、修正結果を誤判定した。
- 改善: 必要時は旧展開物削除とサービス再起動を行い、静的状態を破棄する。

## プロジェクト固有ルール

### GWT / クライアント
- クライアント変更後は GWT 再コンパイル成功を完了条件に含める。
- 依存関係を更新した場合は、`mvn "-Dgwt.skipCompilation=false" gwt:compile` の成功を確認してから完了とする。
- 基盤クラス（例: `StatusUpdater`）変更時は、`@Override` エラー連鎖を関連画面まで確認する。
- `cache.js` を更新した場合、配備先が最新成果物を参照していることを確認する。
- 依存関係は安定版のみ使用し、RC / alpha / milestone 版は採用しない。
- `name.pehl:piriti-user` と `name.pehl:piriti-dev` は `0.8` に固定し、GWT 再コンパイル検証なしで更新しない。
- `gwt:compile` が失敗した場合はデプロイを中断し、`-SkipBuild` による回避配備を行わない。

### Jetty / Tomcat / クラスローダ
- DevMode と Tomcat でクラスローダ挙動が異なる前提で検証する。
- Jetty 関連依存の変更時は、`NoClassDefFoundError` と `ClassCastException` の両方を確認する。
- サーバー初期化失敗時は Guice バインド不足（`No implementation for ...`）を優先確認する。

### WebSocket
- WebSocket URL は実行中の `protocol / host / contextPath` から導出し、環境固定値に依存しない。
- 接続障害時は次の順で確認する。
1. 待受確認（`netstat`）
2. HTTP 到達確認（通常 GET）
3. Upgrade ハンドシェイク確認（`curl.exe`）
4. サーバーログの例外確認（500/503、初期化失敗）
- クライアントログには接続先 URL を含め、追跡可能な形で出力する。

### デプロイ / ローカル運用
- Tomcat 再配備時は、必要に応じて旧展開物削除とサービス再起動で静的状態を確実に破棄する。
- Eclipse で不整合が疑われる場合は、`target` と `gwt-unitCache` のクリーンを実施する。
- 検証（`build` / `test` / `gwt:compile`）が1つでも失敗した場合はデプロイを中断し、修正と再検証完了まで配備しない。

### Git / worktree 運用
- worktree ブランチを `master` に取り込むときは、必ず fast-forward merge（`git merge --ff-only`）を使用する。

## 禁止事項
- 依存スコープ変更を、影響評価と実機検証なしで反映しない。
- WebSocket エラーを、サーバーログ未確認のままクライアント側だけで断定しない。
## 追加の振り返り（2026-02-10, QMAClone）
- ミス: Google連携設定画面で、初期ロード時の一覧表示と「Googleログインして紐づけ済みユーザーコードを表示する」押下後の一覧表示を同一状態として扱い、選択したユーザーコードに切り替える の表示条件が揺れた。
- 改善: 設定画面の一覧表示は「初期ロード由来」と「ユーザー操作由来」で分岐を分け、Presenterのコールバックも分離して実装する。

### 設定画面UX（Google連携）
- onLoad 由来の表示と、showUserCodeList 押下由来の表示は別フローとして扱う。
- 1件表示時の切り替えボタンは、初期ロードでは非表示、ユーザー操作由来では表示を基本とする。
## 追加の振り返り（2026-02-11, QMAClone）
- ミス: JUnit5移行時に `@Rule` / GuiceBerry 依存テストを一括で Jupiter 化し、DI 初期化が動かず `null` 参照を大量発生させた。
- 改善: JUnit移行は `@Rule` 有無で対象を分離し、GuiceBerry 依存テストは Extension 実装完了まで Vintage 実行を維持する。
- ミス: 移行可否判定に、ネイティブ依存（`zinnia.dll` 必須）を持つテストを混在させ、失敗要因の切り分けが遅れた。
- 改善: 移行検証の基準テストは、ネイティブ依存の有無で先に分離してから実行する。
- ミス: `piriti` 変更時に `gwt:compile` の失敗要因（JAXB / rebind 例外）が揺れ、原因切り分けに時間を要した。
- 改善: `piriti` は `0.8` 固定を維持し、変更検証時は `mvn "-Dgwt.skipCompilation=false" gwt:compile` を単独実行して成否を先に確定する。

### JUnit移行（QMAClone）
- JUnit5移行前に `@Rule` の有無でテストを分類し、`@Rule` 依存テストは別タスクとして扱う。
- GuiceBerry を利用するテストは、Jupiter Extension が未整備なら JUnit4/Vintage のまま維持する。
- 部分実行で `mvn -Dtest=... test` を使う場合、必要に応じて `-DfailIfNoTests=false` を付与し、GWT側の「0件失敗」で誤判定しない。
- `ServicesTest` のようなネイティブライブラリ依存テストは、JUnit移行の正常性判定から分離して扱う。
