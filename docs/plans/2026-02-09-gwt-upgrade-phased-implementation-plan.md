# GWT + 関連ライブラリ更新 実行計画（段階移行）

## 1. 目的
- QMAClone の GWT + 関連ライブラリ更新を、失敗時に戻しやすい段階移行で進める。
- DevMode（Eclipse）と Tomcat の両実行系を維持する。
- 変更ごとの検証結果を残し、後から判断理由を再現できる状態にする。

## 2. 前提環境
- 主利用 Eclipse: `C:\Users\nodchip\eclipse\jee-2025-12\eclipse`
- 旧 Eclipse: `C:\home\application\eclipse`（比較・参照用）
- JDK: Java 25
- Maven compiler: `source=25` / `target=25`
- 実行形態: DevMode + Tomcat の二系統維持

## 3. 進め方（合意事項）
- 方式: 段階移行（安全優先）
- 変更粒度: 小グループ単位
- 更新順序:
1. GWT 本体
2. `gin`
3. `gwt-dnd`
4. `piriti`
5. 高リスク群（`gwt-incubator` / `gwt-plus` / `gwt-visualization`）
- 各ステップで固定ゲートを全通過した場合のみ次へ進む。

## 4. 固定ゲート（各ステップ共通）
1. `mvn -DskipTests compile` 成功
2. `mvn test` 成功
3. `mvn -Pgwt-compile-java25 -DskipTests gwt:compile` 成功

注記:
- Java 25 の `gwt:compile` は強カプセル化対応のため、`-Pgwt-compile-java25` を必須とする。
- 手動 `JAVA_TOOL_OPTIONS` ではなく Maven プロファイルで統一する。

## 5. フェーズ詳細

### Phase 0: ベースライン固定
1. 現行 HEAD の依存・ビルド・実行状況を記録する。
2. 既知ワーニング（DevMode/Tomcat）を記録する。
3. 以降はこの記録との差分で評価する。

### Phase 1: GWT 本体
1. `gwt-user` / `gwt-dev` / `gwt-servlet` を更新対象とする。
2. 変更は依存定義とビルド設定を優先し、機能コード変更は最小限にする。
3. 固定ゲートを通過したらコミットする。

### Phase 2: 中リスク群（小グループ）
1. `gin` を更新し、DI 生成とバインド回帰を確認する。
2. `gwt-dnd` を更新し、UI イベント回帰を確認する。
3. `piriti` を更新し、JSON マッピングと generator 回帰を確認する。
4. 各ライブラリごとに固定ゲート通過後にコミットする。

### Phase 3: 高リスク群
1. `gwt-incubator` / `gwt-plus` / `gwt-visualization` を個別サブステップで扱う。
2. 互換問題発生時は当該ライブラリのみ直前コミットへ即時ロールバックする。
3. 他ライブラリの進行は止めず、問題を局所化する。

### Phase 4: 最終統合検証
1. 固定ゲートを再実行する。
2. DevMode/Tomcat で主要導線を通しで確認する。
3. 結果を計画書または検証記録へ反映する。

## 6. 実行コマンド標準
```powershell
mvn -DskipTests compile
mvn test
mvn -Pgwt-compile-java25 -DskipTests gwt:compile
```

## 7. 実行時チェックリスト
- DevMode: `http://127.0.0.1:8888/QMAClone.html`
- Tomcat（Windows）: ローカル配備URL
- Tomcat（Ubuntu）: 本番/検証URL
- 確認項目:
  - 初期画面が表示される
  - タブ内部が表示される
  - RPC が成功する
  - WebSocket が `101 Switching Protocols` になる

## 8. ロールバック規則
- 1コミット1目的で分割し、`git revert` で戻せる単位にする。
- 高リスク群で失敗した場合は「対象ライブラリのみ」ロールバックする。
- 全体を止めるのは、固定ゲート不通過が連鎖し原因切り分け不能な場合のみとする。

## 9. 完了条件
- すべての更新ステップで固定ゲートを通過している。
- DevMode/Tomcat の主要導線確認が完了している。
- ログと判定記録（継続/ロールバック）が追跡可能な形で残っている。
