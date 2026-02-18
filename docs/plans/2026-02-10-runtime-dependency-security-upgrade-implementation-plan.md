# Runtime Dependency Security Upgrade Implementation Plan

## Status
- 状態: 中止（2026-02-18）
- 理由: ユーザー判断により本計画は中止。
- 補足: 再開する場合は本書を直接継続せず、新規計画として起票する。

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** runtime依存5件をセキュリティ優先で更新し、既存機能を壊さずにローカルTomcat配備まで完了する。

**Architecture:** `pom.xml` の対象5依存のみを一括更新し、`compile -> test -> package -> deploy` を直列で実行して回帰を確認する。失敗時は依存単位で切り戻し、対象外依存は変更しない。更新結果と検証ログを専用ドキュメントに記録する。

**Tech Stack:** Maven, Java 25, GWT 2.12.2, Tomcat 9, JUnit4/Mockito

---

### Task 1: 事前ベースラインの固定

**Files:**
- Modify: `pom.xml`
- Create: `docs/plans/2026-02-10-runtime-dependency-security-upgrade-verification.md`

**Step 1: 依存ベースラインを出力する**

Run:
```powershell
mvn -q -DskipTests dependency:tree > target/dependency-tree-before-runtime-security-upgrade.txt
```
Expected: `target/dependency-tree-before-runtime-security-upgrade.txt` が作成される。

**Step 2: 更新対象5依存の現行値を記録する**

Run:
```powershell
rg -n "mysql-connector-java|google-http-client|commons-dbcp2|commons-pool2|slf4j-jdk14" pom.xml -S
```
Expected: 5依存の現行version行が確認できる。

**Step 3: 検証記録ファイルの雛形を作る**

`docs/plans/2026-02-10-runtime-dependency-security-upgrade-verification.md` に以下を作成:
```md
# Runtime Dependency Security Upgrade Verification

## Updated Dependencies
- mysql-connector-java: old -> new
- google-http-client: old -> new
- commons-dbcp2: old -> new
- commons-pool2: old -> new
- slf4j-jdk14: old -> new

## Commands
- [ ] mvn -DskipTests compile
- [ ] mvn -Dtest=PanelSettingUserCodePresenterTest test
- [ ] mvn package -DskipTests
- [ ] deploy_qmaclone_tomcat9.ps1 -SkipBuild

## Manual Checks
- [ ] 設定画面表示
- [ ] ユーザーコード切替
- [ ] Google連携表示/解除
```

**Step 4: 変更をコミットする**

```powershell
git add target/dependency-tree-before-runtime-security-upgrade.txt docs/plans/2026-02-10-runtime-dependency-security-upgrade-verification.md
git commit -m "runtime依存更新の事前ベースラインを記録"
```

### Task 2: 依存5件の一括更新

**Files:**
- Modify: `pom.xml`

**Step 1: 更新後version候補を決める**

Run:
```powershell
mvn -q versions:display-dependency-updates > target/dependency-updates-report.txt
```
Expected: `target/dependency-updates-report.txt` に候補が出力される。

**Step 2: 5依存のみversionを更新する**

`pom.xml` の以下のみ更新:
- `mysql-connector-java`
- `google-http-client`
- `commons-dbcp2`
- `commons-pool2`
- `slf4j-jdk14`

**Step 3: 更新対象外が混ざっていないことを確認する**

Run:
```powershell
git diff -- pom.xml
```
Expected: 5依存のversion差分のみである。

**Step 4: 変更をコミットする**

```powershell
git add pom.xml target/dependency-updates-report.txt
git commit -m "runtime依存5件をセキュリティ優先で更新"
```

### Task 3: 直列検証（build -> test -> package）

**Files:**
- Modify: `docs/plans/2026-02-10-runtime-dependency-security-upgrade-verification.md`

**Step 1: compileを実行する**

Run:
```powershell
$env:JAVA_HOME='C:\Program Files\Java\jdk-25.0.2'
$env:JDK_JAVA_OPTIONS='--add-opens=java.base/java.lang=ALL-UNNAMED --add-opens=java.base/java.lang.reflect=ALL-UNNAMED --add-opens=java.base/java.io=ALL-UNNAMED'
mvn -DskipTests compile
```
Expected: BUILD SUCCESS

**Step 2: 対象テストを実行する**

Run:
```powershell
mvn -Dtest=PanelSettingUserCodePresenterTest test
```
Expected: BUILD SUCCESS, Tests run > 0, Failures=0, Errors=0

**Step 3: packageを実行する**

Run:
```powershell
mvn package -DskipTests
```
Expected: BUILD SUCCESS, WAR生成成功

**Step 4: 検証記録を更新する**

`docs/plans/2026-02-10-runtime-dependency-security-upgrade-verification.md` に実行結果と日時を追記する。

**Step 5: 変更をコミットする**

```powershell
git add docs/plans/2026-02-10-runtime-dependency-security-upgrade-verification.md
git commit -m "runtime依存更新の機械検証結果を記録"
```

### Task 4: ローカル配備と手動確認

**Files:**
- Modify: `docs/plans/2026-02-10-runtime-dependency-security-upgrade-verification.md`

**Step 1: Tomcat9へ配備する**

Run:
```powershell
powershell -ExecutionPolicy Bypass -File .\deploy_qmaclone_tomcat9.ps1 -SkipBuild
```
Expected: `Done.` が出力される。

**Step 2: 手動確認を実施する**

確認項目:
- 設定画面が表示される
- ユーザーコード切替が動作する
- Google連携表示/解除フローが動作する

**Step 3: 検証記録を更新する**

`docs/plans/2026-02-10-runtime-dependency-security-upgrade-verification.md` に手動確認結果を追記する。

**Step 4: 変更をコミットする**

```powershell
git add docs/plans/2026-02-10-runtime-dependency-security-upgrade-verification.md
git commit -m "runtime依存更新後のローカル配備確認を記録"
```

### Task 5: 失敗時の切り戻し

**Files:**
- Modify: `pom.xml`
- Modify: `docs/plans/2026-02-10-runtime-dependency-security-upgrade-verification.md`

**Step 1: 失敗した依存を特定する**

Run:
```powershell
git diff -- pom.xml
```
Expected: 更新5依存の差分が把握できる。

**Step 2: 失敗依存のみ旧versionへ戻す**

`pom.xml` で該当依存のみversionを戻す。

**Step 3: compile -> test を再実行する**

Run:
```powershell
mvn -DskipTests compile
mvn -Dtest=PanelSettingUserCodePresenterTest test
```
Expected: BUILD SUCCESS

**Step 4: 切り戻し内容を記録してコミットする**

```powershell
git add pom.xml docs/plans/2026-02-10-runtime-dependency-security-upgrade-verification.md
git commit -m "runtime依存更新の互換性問題に対して部分切り戻し"
```
