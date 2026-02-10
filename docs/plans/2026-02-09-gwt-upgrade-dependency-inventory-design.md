# GWT + 関連ライブラリ更新 棚卸し設計（段階移行）

## 1. 目的と方針

- 目的:
  - QMAClone の `GWT + 関連ライブラリ` を更新し、将来のビルド/実行不整合リスクを下げる。
  - 既存機能（DevMode / Tomcat / WebSocket / 既存画面）を壊さずに更新する。
- 方針:
  - 一括更新ではなく段階移行で進める。
  - まず依存棚卸しと難易度分類を行い、高リスク領域から検証駆動で更新する。
  - 完了条件は「ビルド成功 + GWT コンパイル成功 + DevMode/Tomcat 主要導線確認」。

## 2. 更新対象（現状）

- GWT 本体:
  - `com.google.gwt:gwt-user` / `gwt-servlet` / `gwt-dev`（BOM `com.google.gwt:gwt:2.9.0`）
- 関連ライブラリ:
  - `com.google.gwt.inject:gin:2.1.2`
  - `com.allen-sauer.gwt.dnd:gwt-dnd:3.3.4`
  - `com.google.gwt:gwt-incubator:2.0.1`
  - `name.pehl:piriti-user:0.7`
  - `local.legacy:gwt-visualization:1.1.2`（system scope）
  - `local.legacy:gwt-plus:1-0.3-alpha`（system scope）
  - `local.legacy:gwt-web-sockets:1.0.0`（system scope）

## 3. 利用箇所と難易度

- 高:
  - `gwt-incubator`（`GWTCanvas` 系で広範囲利用）
  - `gwt-plus`（Google API 連携）
  - `gwt-visualization`（統計系 UI）
- 中:
  - `gwt-web-sockets`（`StatusUpdater` 経由で利用）
  - `gin`（依存注入境界）
  - `gwt-dnd`（入力 UI の DnD）
- 低:
  - `piriti`（JSON マッピング、置換可能性ありだが利用範囲は比較的限定）

## 4. 比較した進め方

- 案A: 一括更新
  - 利点: 期間が短く見える
  - 欠点: 失敗時の切り分けが困難
- 案B: コアのみ先行（GWT 本体だけ）
  - 利点: 影響範囲が限定される
  - 欠点: 周辺ライブラリとの整合で詰まりやすい
- 案C: 段階移行（推奨）
  - 利点: 失敗時の切り戻しと原因特定が容易
  - 欠点: ステップ管理が必要

推奨は案C。既存運用（DevMode/Tomcat 両立）を維持しながら安全に進められるため。

## 5. 実施ステップ（設計）

1. ベースライン固定
   - 現行 HEAD で `mvn -DskipTests compile`、GWT コンパイル、DevMode/Tomcat 動作を記録。
2. GWT 本体更新
   - `gwt-user/gwt-dev/gwt-servlet` を候補版へ更新し、コンパイルと主要画面を確認。
3. 中リスク群更新
   - `gin`、`gwt-dnd`、`gwt-web-sockets` を順番に更新。
   - 各更新ごとにビルド/起動/主要導線を再検証。
4. 高リスク群更新
   - `gwt-incubator` / `gwt-plus` / `gwt-visualization` の代替・互換確認を伴って更新。
   - 必要なら段階的に API 置換を入れる。
5. 仕上げ
   - `system scope` 依存の削減方針を別タスク化。

## 6. 検証観点

- ビルド:
  - `mvn -DskipTests compile`
  - GWT コンパイル（最新 `cache.js` 生成確認）
- 実行:
  - DevMode: `http://127.0.0.1:8888/QMAClone.html`
  - Tomcat: 配備後の初期画面/タブ表示/WebSocket 接続
- 回帰:
  - ログイン、ロビー、ゲーム遷移、チャット、統計表示

## 7. 受け入れ条件

- 主要ライブラリ更新後も DevMode/Tomcat の双方で致命的不具合がない。
- WebSocket/RPC が継続動作する。
- GWT コンパイルが安定し、旧成果物配信が起きない。
