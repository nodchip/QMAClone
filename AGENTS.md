# AGENTS.md（QMAClone）

## 適用範囲
- このファイルは QMAClone 固有ルールのみを扱う。
- 共通ルールは `C:\Users\nodchip\.codex\AGENTS.md` を参照する。

## 目的
- QMAClone で再発した障害を、環境差分（DevMode / Jetty / Tomcat / GWT / WebSocket）を前提に再発防止する。

## 参照ドキュメント
- GWT 調査の起点: `docs/gwt-guide/README.md`
- UI/UX スタイルガイド: `docs/ui-ux-style-guide.md`

## 過去の作業ミス（QMAClone 固有）と改善案
- ミス: DevMode と Tomcat の差を考慮せず、クラスローダ起因の `NoClassDefFoundError` / `ClassCastException` を見落とした。
- 改善: Jetty 関連変更時は両例外をセットで確認し、DevMode と Tomcat で個別に起動検証する。
- ミス: GWT 再コンパイル失敗後に古い `cache.js` が配信され、画面不整合が起きた。
- 改善: クライアント変更の完了条件に「GWT 再コンパイル成功」と「配備先で最新成果物が参照されること」を含める。
- ミス: GWT クライアントで非対応 API（例: `String.format`）を使用し、デプロイ時の `gwt:compile` で停止した。
- 改善: クライアント変更では GWT JRE エミュレーション非対応 API を使わず、`deploy_qmaclone_tomcat10.ps1` 実行前に `gwt:compile` 成功を確定する。
- ミス: CellTable の見た目修正で `.cellTableCell` 固定セレクタを使い、実DOM（難読化クラス構成）に一致せずスタイルが未適用になった。
- 改善: CellTable 系CSSは `styleName + 要素セレクタ(td/button など)` を基本にし、`dom.html` で実セレクタ一致を確認してから反映する。
- ミス: WebSocket URL を環境固定値で扱い、接続先不一致（404/500）を誘発した。
- 改善: URL は `protocol / host / contextPath` から導出し、クライアントログに接続先 URL を必ず含める。
- ミス: WebSocket 障害をサーバーログ確認前に断定し、調査が遠回りになった。
- 改善: `netstat -> HTTP GET -> Upgrade ハンドシェイク -> サーバーログ` の順で切り分ける。
- ミス: Tomcat 再配備時に旧状態が残り、修正結果を誤判定した。
- 改善: 必要時は旧展開物削除とサービス再起動を行い、静的状態を破棄する。
- ミス: nighthawk の reverse proxy 先サービス停止を見落とし、`502 Bad Gateway` を nginx 設定起因と誤認した。
- 改善: `nginx error.log` と upstream 待受/サービス状態を先に確認し、停止サービスを復旧してから設定変更要否を判断する。

## プロジェクト固有ルール

### GWT / クライアント
- クライアント変更後は GWT 再コンパイル成功を完了条件に含める。
- 依存更新時は `mvn "-Dgwt.skipCompilation=false" gwt:compile` を単独実行して成否を先に確定する。
- 基盤クラス（例: `StatusUpdater`）変更時は、`@Override` エラー連鎖を関連画面まで確認する。
- `cache.js` を更新した場合、配備先が最新成果物を参照していることを確認する。
- キーボード入力の制御キー（Backspace/Enter など）は `keypress` に依存せず `keydown` で拾う（環境差で `keypress` が発火しない場合がある）。
- 依存関係は安定版のみ使用し、RC / alpha / milestone 版は採用しない。
- `piriti` 依存は再導入せず、クライアントの JSON デコードは明示実装を維持する。
- `gin` は `de.knightsoft-net:gin:4.0.0` を基準とし、Guice 更新時は GWT rebind 成否を先に確認する。
- GWT クライアント（`client` 配下）では JRE の汎用 API を無条件に使わず、`String.format` など GWT 非対応 API の利用前にエミュレーション対応可否を確認する。`gwt:compile` 失敗時はデプロイを中断し、`-SkipBuild` による回避配備を行わない。

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
- 修正を含む依頼では、実行成果物に影響する変更（サーバー/クライアント/CSS/配備スクリプト）を行ったら、完了報告前に `deploy_qmaclone_tomcat10.ps1` でデプロイする（ユーザーが「デプロイ不要」を明示した場合のみ省略可）。旧 `deploy_qmaclone_tomcat9.ps1` は互換ラッパーとしてのみ使用する。
- デプロイ後は公開経路のHTTP疎通を確認し、実行コマンドと結果を完了報告に残す。
1. `/` の `HTTP 200`（nginx -> apache 経路）
2. `/QMAClone/` の `HTTP 200`（nginx -> tomcat 経路）
3. `/QMAClone/tv.dyndns.kishibe.qmaclone.QMAClone/service` の `HTTP 405`
4. `/QMAClone/tv.dyndns.kishibe.qmaclone.QMAClone/service?warmup=1` の `HTTP 200`
5. `/QMAClone/websocket` の Upgrade 疎通（`101 Switching Protocols` もしくはサーバーログで Upgrade 成功を確認）
- 新規の運用ログ/メモはルート直下へ置かず、`ops/log/` と `ops/notes/` 配下へ配置する。
- 新規/更新の運用補助スクリプトは `ops/scripts/` 配下へ配置し、既存ルートスクリプトは段階移行で扱う。

### Nginx / Upstream 運用（nighthawk）
- `502/504` 調査は `nginx error.log` -> upstream 待受 (`ss -ltnp`) -> upstream サービス状態 (`systemctl status`) の順で実施する。
- Python/Node などアプリ系 upstream 復旧時は依存ランタイム（`venv` / パッケージ / 実行ユーザー）まで確認し、`systemctl restart` のみで終わらせない。

### Git / worktree 運用
- worktree ブランチを `master` に取り込むときは、必ず fast-forward merge（`git merge --ff-only`）を使用する。
- UI全体改修のような広範囲変更は、`テーマ基盤` / `共通部品` / `画面別適用` / `文言統一` の単位でコミットを分離し、1コミット1目的を維持する。
- worktree 取り込み前は `git status --short` で `AGENTS.md` と `docs/plans/*implementation-plan*.md` の未整理差分有無を確認し、取り込み対象外の変更を混在させない。

## 禁止事項
- 依存スコープ変更を、影響評価と実機検証なしで反映しない。
- WebSocket エラーを、サーバーログ未確認のままクライアント側だけで断定しない。
- `docs/ui-ux-style-guide.md` と矛盾する UI/UX 実装を、理由と代替案の明記なしで導入しない。

## 再発防止ルール（QMAClone カテゴリ別）

### 設定画面UX（Google連携）
- onLoad 由来と `showUserCodeList` 押下由来を別フローで扱い、Presenter のコールバックを分離する。
- 「選択したユーザーコードに切り替える」ボタンは、1件表示でも起点で制御する（初期ロードは非表示、ユーザー操作由来は表示）。

### JUnit移行とテスト分離
- JUnit5移行前に `@Rule` の有無でテストを分類し、`@Rule` 依存テストは別タスクとして扱う。
- GuiceBerry 利用テストは Jupiter Extension 未整備なら JUnit4/Vintage のまま維持する。
- 移行可否判定はネイティブ依存有無で分離し、`ServicesTest` のようなネイティブ依存テストは正常性判定から分離する。
- 部分実行で `mvn -Dtest=... test` を使う場合は、必要に応じて `-DfailIfNoTests=false` を付与する。

### WebSocket移行時の置換範囲
- 自動置換は `src/main/java/tv/dyndns/kishibe/qmaclone/server/websocket` と `src/test/java/tv/dyndns/kishibe/qmaclone/server/websocket` のみに適用する。
- `src/main/java/net/zschech/gwt/websockets` は GWT クライアント基盤として扱い、置換対象に含めない。

### GWTコンパイル・テストの安定化
- 変更範囲が限定される場合は対象パッケージのテストを先に実行し、全量テスト未完了時は未検証範囲を完了報告に明記する。
- 開発中の通常テスト実行は `mvn "-Dsurefire.skip=false" test` を基本とし、`server/database` 配下の重いテストは既定除外のまま実行時間を抑える。
- DB関連の変更、またはリリース前の全量検証では `mvn "-Dsurefire.skip=false" -Pwith-db-tests test` を実行し、除外されたDBテストを必ず再有効化する。
- `ValidatorTegaki` 系テストは `AvailableCharacters` を固定化し、`Service.getAvailableChalactersForHandwriting` を直接呼ばない。
- `forked VM terminated` 発生時は `surefire-reports` の実行済み差分から停止クラスを特定し、外部依存（RPC / ネイティブ / ファイル）を優先的に切り離す。

### RPC失敗ハンドリング統一
- `client` 配下のRPCコールバック実装は `RpcAsyncCallback` を使用し、`new AsyncCallback<...>()` を直接実装しない。
- `RpcAsyncCallback` の `onFailure` で `ClientReloadPrompter.maybePrompt` を実行するため、`onFailureRpc` 側で同処理を重複呼び出ししない。
- 旧GWTキャッシュ判定で画面固有の早期終了が必要な場合は、`ClientReloadPrompter` ではなく `StaleRpcFailureDetector` を参照して分岐する。

### UI/UX実装ガード（全画面共通）
- UI/UX 判断が必要な変更は、`docs/ui-ux-style-guide.md` を仕様として扱う。
- スタイル不一致を修正する前に、対象要素の実DOMと実効スタイル（最終適用セレクタ）を確認する。
- `width: 100%` 要素へ `padding` を与える場合は、`box-sizing: border-box` をセットで指定する。
- GWT 難読化クラス（例: `GG-*`）への直接依存を恒久対応にしない。`styleName` または安定クラスを使う。
- レイアウト変更時は、デスクトップ通常幅・中間幅・モバイル幅の3状態で表示崩れと重なりを確認する。
- 折り返し（`flex-wrap`）など div 前提のCSSを適用する場合、コンテナは `HorizontalPanel` / `VerticalPanel`（table生成）ではなく `FlowPanel` 等の div 生成ウィジットを使う。適用前に実DOM（`dom.html` 等）で table/div を確認する。

### チャット折りたたみ運用
- 表示切替UIは CSS 固定非表示と Java 表示制御を重ねず、非表示条件を一方へ統一する。
- 右チャットの折りたたみ状態は `Controller` と `localStorage` キーで管理する。
- `PanelChat` にヘッダー文言を重複配置しない。ヘッダーとトグルは `Controller` 側に集約する。
- 折りたたみ時は再表示トリガ（例: 「チャットを開く」ボタン）を常時視認可能にする。
- チャットのレイアウト崩れを調査する場合は、`html.html` などで実DOMを採取し、UiBinder の要素型（`table` / `div`）と CSS 前提（`display:flex` など）が一致していることを先に確認する。
- UiBinder が `VerticalPanel` / `FlexTable` を生成する箇所では、`table` 要素に `display:flex` を適用しない。必要な柔軟レイアウトは内側の `FlowPanel` など `div` 要素に限定する。
- レイアウト変更時は「通常表示」「折りたたみ表示」「折りたたみ解除」の3状態をスクリーンショットで確認する。
- チャット位置・サイズを変更した場合は、横幅を段階的に縮めて「デスクトップ固定表示」「下部ドロワー移行直前」「下部ドロワー移行後」の3幅で中央パネルとの重なりがないことを完了条件に含める。
- 右下固定の通知UIを変更した場合は「デスクトップ通常表示」「チャット展開表示」「モバイル幅表示」の3状態で表示位置を確認し、意図しない `top` / `bottom` 上書きがないことを完了条件に含める。

### ロビーUIスタイル適用
- UiBinder の `ui:style`（CssResource）で GWT 標準クラス（例: `.gwt-TextBox` / `.gwt-ListBox` / `.gwt-CheckBox`）を直接指定しない。必要な場合は専用 `styleName` を付与するか、`input[type="text"]` / `select` など要素セレクタで適用する。
- 表示文言の折り返しを制御したい箇所（指標ラベルなど）は、1項目ごとに要素を分割し、`white-space: nowrap` を項目単位で適用する。
- ロビー画面のCSS変更時は「主要設定」「詳細設定」「下部サマリー」の3ブロックを同一画面幅で確認し、枠幅の不一致がないことを完了条件に含める。

### フォームラベル整列
- `Grid` / `FlexTable` ベースの入力フォームは、ラベルセルと値セルをともに `vertical-align: middle` で統一する。
- ラベルを右寄せにするフォームでは、入力欄との視認距離を保つため `padding-right` を明示し、0にしない。
- チェックボックス/ラジオボタンの「ラベル + 右側テキスト」は `inline-flex` と `align-items: center` を基本とし、入力要素とテキストの縦位置差を残さない。
- `RadioButton` 行で左右要素の縦位置が崩れる場合は、`input` 単体ではなく `.gwt-RadioButton` コンテナ自体に `display: flex` と `align-items: center` を適用し、`label` 側は `flex: 1` で幅を確保して中央揃えを維持する。

### GWTセル系UIスタイル適用
- `CellBrowser` / `CellList` の選択状態を CSS で調整する場合、GWT再コンパイルで変化する難読化クラス（例: `GG-*` / `*WCGB*`）を直接セレクタに使わない。
- セル選択色は `CellList.Resources`（`CssResource`）や `setStyleName` で付与した安定クラスで制御し、`!important` 付き難読化クラス上書きを恒久対応にしない。
- スタイル不一致の切り分けでは、まず `dom.html` などに実DOMを保存し、`gwt-*` クラスか難読化クラスかを確認したうえで修正方針（安定クラス化 / 部品置換）を決める。
- CellTable のボタン/セル装飾は `.cellTableCell` 固定を避け、`<table styleName> td button` のように実DOMへ一致する安定セレクタを優先する。

### ゲーム画面プレイヤー一覧レイアウト
- `AbsolutePanel` 配下のカードUIを変更する場合は、`親幅 = 子要素width + left余白 + right余白` を満たすように `setPixelSize` / `OFFSET_X` / CSS `width` を同時に見直す。
- カード本体と重ね表示ラベル（`answerPopup`）は、`left` と `width` を同一基準でそろえ、`box-sizing: border-box` を指定して境界線分のはみ出しを防ぐ。
- 「右寄り」「はみ出し」調査ではスクリーンショットだけで判断せず、`dom.html` を採取して対象要素の inline style（`left` / `width`）と適用クラスを確認してから修正する。
