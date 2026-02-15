# Maven標準化向け資産棚卸し結果

## 実施概要
- 実施日: 2026-02-16
- 対象: `src/**`
- 除外: `src/main/gwt-unitCache/**`（生成物）
- 分類軸: 拡張子ベース
- 出力粒度: 件数 + 代表パス（3〜5件）

## 拡張子集計（全体）
| 拡張子 | 件数 | 代表パス（抜粋） |
|---|---:|---|
| `.java` | 524 | `src/main/java/tv/dyndns/kishibe/qmaclone/client/ClientReloadPrompter.java` / `src/main/java/tv/dyndns/kishibe/qmaclone/client/Controller.java` / `src/main/java/tv/dyndns/kishibe/qmaclone/client/GlobalKeyEventHandler.java` |
| `.gif` | 22 | `src/main/java/tv/dyndns/kishibe/qmaclone/client/lib/text/backColors.gif` / `src/main/java/tv/dyndns/kishibe/qmaclone/client/lib/text/bold.gif` / `src/main/java/tv/dyndns/kishibe/qmaclone/client/lib/text/createLink.gif` |
| `.xml` | 16 | `src/main/java/tv/dyndns/kishibe/qmaclone/QMAClone.gwt.xml` / `src/main/java/tv/dyndns/kishibe/qmaclone/client/chat/PanelPast.ui.xml` / `src/main/webapp/WEB-INF/web.xml` |
| `.png` | 4 | `src/main/webapp/back.png` / `src/main/webapp/notification_error.png` / `src/main/webapp/notification_resolved.png` |
| `.svg` | 3 | `src/main/webapp/notification_error.svg` / `src/main/webapp/notification_resolved.svg` / `src/main/webapp/notification_warning.svg` |
| `.css` | 2 | `src/main/java/tv/dyndns/kishibe/qmaclone/client/report/CellTableProblem.css` / `src/main/webapp/QMAClone.css` |
| `.html` | 2 | `src/main/webapp/creation-guideline.html` / `src/main/webapp/QMAClone.html` |
| `.properties` | 1 | `src/main/java/tv/dyndns/kishibe/qmaclone/client/lib/text/RichTextToolbar_Strings.properties` |

## 詳細分類（`.xml`）
- `*.ui.xml`: 11件
- `*.gwt.xml`: 4件
- その他 `.xml`: 1件（`src/main/webapp/WEB-INF/web.xml`）

## 移動計画（2分類 + 推奨移動先）
以下は「`src/main/java` と `src/test/java` にある非 `.java`」を対象にした移動計画。

### 今すぐ移動可
今回の棚卸し結果では **0件**。  
`src/main/java` 配下の非Java資産は、GWT rebind/UiBinder/相対参照の影響があり得るため、全件を要検証扱いにした。

### 要検証
| 対象 | 件数 | 判定理由 | 推奨移動先 |
|---|---:|---|---|
| `.gif`（`src/main/java`） | 22 | Client側UI資産で参照経路が埋め込み実装に依存する可能性 | `src/main/resources`（同一パッケージ構造を維持） |
| `.xml`（`src/main/java` / `src/test/java`） | 15 | `*.ui.xml` / `*.gwt.xml` は GWT コンパイル時参照の影響が大きい | `src/main/resources` / `src/test/resources`（要段階検証） |
| `.css`（`src/main/java`） | 1 | CssResource/ClientBundle 参照の可能性 | `src/main/resources`（同一パッケージ構造を維持） |
| `.properties`（`src/main/java`） | 1 | i18n バンドル参照の経路確認が必要 | `src/main/resources`（同一パッケージ構造を維持） |

## 移動対象外（現状維持）
以下は Maven 標準位置にあり、今回の移動対象外とする。
- `src/main/webapp/**` の `.png` / `.svg` / `.html` / `WEB-INF/web.xml`
- `.java` 全件

## 次アクション
1. `要検証` の中から低リスク群（例: `.properties` 単体）を最小単位で移動する実施計画を作成する。
2. 各移動単位ごとに `mvn compile`、必要時 `mvn "-Dgwt.skipCompilation=false" gwt:compile` で検証する。
3. 1コミット1目的で段階的に適用する。
