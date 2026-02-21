# Nighthawk Ubuntu24.04 + Tomcat10 Release Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** nighthawk を Ubuntu 24.04 へ更新し、Tomcat10 (apt 最新) で QMAClone を稼働させつつ、GWT コンパイルを Pretty/非ドラフト/高最適化で固定し、不要サービス削除とセキュリティ・プライバシー是正を完了する。

**Architecture:** 移行は「事前バックアップ -> Jakarta 互換化 -> 検証済み成果物作成 -> OS/ミドル更新 -> Tomcat10 配備 -> nginx/WebSocket 切替 -> セキュリティ是正 -> 疎通検証」の直列で実施する。Tomcat10 強制に伴う `javax.*` 依存は先にコード側で排除し、サーバー停止時間は WAR 差し替えと設定反映の最小区間に限定する。失敗時は取得済みバックアップから Jammy + Tomcat9 構成へ戻せるようロールバック手順を同時に保持する。

**Tech Stack:** Ubuntu 24.04 LTS, Tomcat10, nginx, MySQL, AppArmor/systemd, Maven, GWT 2.13, Java 25 (Oracle JDK)

---

## 進捗メモ（2026-02-21 ローカル限定）

- 完了:
1. Task2 途中まで完了（`javax.*` -> `jakarta.*` の主要移行、`mvn compile/test` 成功）
2. Task3 途中まで完了（本番ビルド方針: PRETTY/非ドラフト/optimize=9、Linux配備スクリプト追加）
- 対応済みの重要課題:
1. `gwt:test` での `LinkServlet` `ClassCastException` は、`QMAClone.gwt.xml` の `<servlet>` 宣言削除で解消
2. 根拠: GWT 2.13 `JUnitShell` が `javax.servlet.Servlet` を前提に `asSubclass` するため
- 未完了:
1. 開発機 Tomcat10 導入（`choco install tomcat10 -y` は管理者権限不足で未完）
2. 本番サーバー側 Task5 以降（ユーザー方針どおり未着手）

---

### Task 1: 事前棚卸しとバックアップ固定

**Files:**
- Create: `ops/notes/2026-02-21-nighthawk-release-inventory.md`
- Create: `ops/log/2026-02-21-nighthawk-preflight.log`
- Modify: `docs/plans/2026-02-21-nighthawk-noble-tomcat10-release-implementation-plan.md`

**Step 1: 現状採取コマンドを実行してログ化**

Run:
```bash
ssh root@nighthawk 'hostnamectl; cat /etc/os-release; java -version; nginx -v; mysql --version'
ssh root@nighthawk 'systemctl list-units --type=service --state=running'
ssh root@nighthawk 'ls -la /var/lib/tomcat9/webapps; ls -la /etc/nginx/sites-enabled'
```

Expected: 現在の構成が `ops/log/2026-02-21-nighthawk-preflight.log` に保存される。

**Step 2: 設定・データのバックアップ取得**

Run:
```bash
ssh root@nighthawk 'mkdir -p /root/backup/qmaclone-$(date +%F)'
ssh root@nighthawk 'tar czf /root/backup/qmaclone-$(date +%F)/etc-nginx.tgz /etc/nginx'
ssh root@nighthawk 'tar czf /root/backup/qmaclone-$(date +%F)/etc-tomcat9.tgz /etc/tomcat9 /var/lib/tomcat9/conf'
ssh root@nighthawk 'mysqldump --single-transaction --routines --triggers -uroot -p QMAClone > /root/backup/qmaclone-$(date +%F)/qmaclone.sql'
```

Expected: 復旧に必要な最小バックアップが揃う。

**Step 3: コミット**

```bash
git add ops/notes/2026-02-21-nighthawk-release-inventory.md docs/plans/2026-02-21-nighthawk-noble-tomcat10-release-implementation-plan.md
git commit -m "docs: nighthawk本番移行の事前棚卸し手順を追加"
```

### Task 2: Tomcat10 対応のための Jakarta 互換化

**Files:**
- Modify: `pom.xml`
- Modify: `src/main/java/**/*.java`（`javax.servlet.*` / `javax.websocket.*` / `javax.annotation.*` 利用箇所）
- Test: `src/test/java/**/*.java`

**Step 1: 失敗を再現（Tomcat10 相当 API で compile）**

Run:
```bash
mvn -q -DskipTests compile
```

Expected: `javax.*` 依存が残っていれば失敗または Tomcat10 配備で実行時失敗要因が確認できる。

**Step 2: Jakarta 依存へ差し替え**

Implementation guideline:
```xml
<!-- 例: servlet/websocket を jakarta に変更 -->
<dependency>
  <groupId>jakarta.servlet</groupId>
  <artifactId>jakarta.servlet-api</artifactId>
  <version>6.0.0</version>
  <scope>provided</scope>
</dependency>
<dependency>
  <groupId>jakarta.websocket</groupId>
  <artifactId>jakarta.websocket-api</artifactId>
  <version>2.1.1</version>
  <scope>provided</scope>
</dependency>
```

Expected: `import javax.*` が `jakarta.*` に統一される。

**Step 3: 検証（直列）**

Run:
```bash
mvn -q compile
mvn -q "-Dsurefire.skip=false" test
```

Expected: compile/test 成功。

**Step 4: コミット**

```bash
git add pom.xml src/main/java src/test/java
git commit -m "refactor: Tomcat10向けにjakarta APIへ移行"
```

### Task 3: GWT コンパイル方針の固定（本番: Pretty + 非ドラフト + 最適化 / 開発機: ドラフト）

**Files:**
- Modify: `pom.xml`
- Modify: `deploy_qmaclone_tomcat9.ps1`
- Create: `ops/scripts/deploy/deploy_qmaclone_tomcat10.sh`

**Step 1: 失敗テスト（現状のコンパイル引数確認）**

Run:
```bash
mvn -q -DskipTests "-Dgwt.skipCompilation=false" gwt:compile
```

Expected: 出力スタイル/最適化が要件未固定であることを確認。

**Step 2: 実装**

- `pom.xml` の `gwt-maven-plugin` に以下を追加:
```xml
<compilerArgs>
  <arg>-style</arg><arg>PRETTY</arg>
  <arg>-optimize</arg><arg>9</arg>
  <arg>-XnoclassMetadata</arg>
  <arg>-XnocheckCasts</arg>
</compilerArgs>
```
- `deploy_qmaclone_tomcat9.ps1` は既定を開発機向け `GwtDraftCompile=$true` のまま維持し、本番配備時のみ `-GwtDraftCompile:$false` を明示して非ドラフト化する。
- Linux 実行用の `ops/scripts/deploy/deploy_qmaclone_tomcat10.sh` を追加し、同じ GWT 方針で WAR を生成する。

**Step 3: 検証**

Run（本番向け非ドラフト検証）:
```bash
mvn -q -Pgwt-compile-java25 -DskipTests "-Dgwt.skipCompilation=false" gwt:compile
mvn -q -DskipTests package
```

Expected: `gwt:compile` が成功し、Pretty 出力の成果物が生成される。

**Step 4: コミット**

```bash
git add pom.xml deploy_qmaclone_tomcat9.ps1 ops/scripts/deploy/deploy_qmaclone_tomcat10.sh
git commit -m "build: GWTコンパイル方針をpretty非ドラフト高最適化へ固定"
```

### Task 4: 開発機 Tomcat10 先行アップグレードとローカル動作確認

**Files:**
- Modify: `deploy_qmaclone_tomcat9.ps1`（必要に応じて Tomcat10 互換化）
- Create: `ops/log/2026-02-21-local-tomcat10-validation.log`

**Step 1: 開発機の Tomcat10 を導入**

Run (PowerShell):
```powershell
choco install tomcat --version=10.1.39 -y
```

Expected: Tomcat10 サービスが導入される。

**Step 2: 既存 Tomcat9 設定を Tomcat10 に移植**

Run (PowerShell):
```powershell
Test-Path "C:\Program Files\Apache Software Foundation\Tomcat 10.1\conf\server.xml"
```

Expected: Tomcat10 の `server.xml` / `webapps` が確認できる。

**Step 3: ローカル配備と疎通確認**

Run (PowerShell, 開発機向けドラフトコンパイル):
```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\deploy_qmaclone_tomcat9.ps1 -ServiceName "Tomcat10" -TomcatBase "C:\Program Files\Apache Software Foundation\Tomcat 10.1" -GwtDraftCompile:$true
curl.exe -s -o NUL -w "%{http_code}" http://localhost:8080/QMAClone-1.0-SNAPSHOT/
curl.exe -s -o NUL -w "%{http_code}" http://localhost:8080/QMAClone-1.0-SNAPSHOT/tv.dyndns.kishibe.qmaclone.QMAClone/service
```

Expected:
- `/QMAClone-1.0-SNAPSHOT/` が `HTTP 200`
- `/service` が `HTTP 405`

**Step 4: コミット**

```bash
git add deploy_qmaclone_tomcat9.ps1 ops/log/2026-02-21-local-tomcat10-validation.log
git commit -m "ops: 開発機Tomcat10でのQMAClone検証手順を追加"
```

### Task 5: Ubuntu 24.04 へアップグレード

**Files:**
- Create: `ops/log/2026-02-21-nighthawk-do-release-upgrade.log`

**Step 1: 事前更新**

Run:
```bash
ssh root@nighthawk 'apt update && apt -y full-upgrade && apt -y autoremove'
```

Expected: 事前更新完了。

**Step 2: リリースアップグレード**

Run:
```bash
ssh root@nighthawk 'do-release-upgrade'
```

Expected: `Ubuntu 24.04.x LTS` へ更新。

**Step 3: 再起動後確認**

Run:
```bash
ssh root@nighthawk 'cat /etc/os-release; uname -a'
```

Expected: `VERSION_ID="24.04"`。

### Task 6: Oracle Java 25 導入 + Tomcat10 移行

**Files:**
- Create: `ops/notes/2026-02-21-java25-tomcat10-migration.md`
- Modify: `/etc/default/tomcat10`（サーバー側）
- Modify: `/etc/systemd/system/tomcat10.service.d/override.conf`（サーバー側）

**Step 1: Java 25 導入と alternatives 更新**

Run:
```bash
ssh root@nighthawk 'dpkg -i jdk-25_linux-x64_bin.deb'
ssh root@nighthawk 'update-alternatives --config java'
```

Expected: `java -version` が 25。

**Step 2: Tomcat10 インストールと設定移植**

Run:
```bash
ssh root@nighthawk 'apt install -y tomcat10 tomcat10-admin'
ssh root@nighthawk 'systemctl stop tomcat9; systemctl disable tomcat9'
```

Expected: 稼働系が `tomcat10` に切替可能状態。

**Step 3: QMAClone 配備と起動**

Run:
```bash
scp target/QMAClone-1.0-SNAPSHOT.war root@nighthawk:/var/lib/tomcat10/webapps/
ssh root@nighthawk 'systemctl daemon-reload; systemctl restart tomcat10'
```

Expected: `/var/lib/tomcat10/webapps/QMAClone-1.0-SNAPSHOT.war` が展開される。

### Task 7: nginx/WebSocket 設定更新 + 不要サービス削除

**Files:**
- Modify: `ops/config/live/nginx/sites-enabled/default`
- Modify: `/etc/nginx/sites-available/default`（サーバー側）

**Step 1: WebSocket と contextPath を新配備名へ統一**

Run:
```bash
# location /QMAClone-1.0-SNAPSHOT/websocket { ... }
# proxy_pass http://127.0.0.1:60080;
```

Expected: `wss://kishibe.dyndns.tv/QMAClone-1.0-SNAPSHOT/websocket/...` が Upgrade 可能。

**Step 2: 削除対象サービスの公開停止**

Run:
```bash
ssh root@nighthawk 'rm -rf /var/lib/tomcat10/webapps/QueenPuzzle-1.0-SNAPSHOT* /var/lib/tomcat10/webapps/TouchTheNumbers-1.0-SNAPSHOT*'
ssh root@nighthawk 'systemctl disable --now director-room laboratory-c'
ssh root@nighthawk 'rm -rf /var/www/html/zetsubou /var/www/html/FourthMission /var/www/html/DirectorRoom /var/www/html/LaboratoryC'
```

Expected: 指定7サービスが到達不能。

**Step 3: nginx 構文確認・反映**

Run:
```bash
ssh root@nighthawk 'nginx -t && systemctl reload nginx'
```

Expected: `syntax is ok`。

### Task 8: 倫理・セキュリティ・プライバシー是正

**Files:**
- Modify: `/etc/nginx/nginx.conf`（TLS/ヘッダー）
- Modify: `/etc/ssh/sshd_config.d/99-hardening.conf`
- Modify: `ufw` ルール
- Create: `ops/notes/2026-02-21-security-remediation.md`

**Step 1: TLS とヘッダー強化**

- `ssl_protocols TLSv1.2 TLSv1.3;`
- `server_tokens off;`
- `Strict-Transport-Security`, `X-Content-Type-Options`, `Referrer-Policy` を追加。

**Step 2: 露出サービス縮小（ローカル限定公開）**

Run:
```bash
ssh root@nighthawk 'ufw delete allow 10000 || true'
ssh root@nighthawk 'ufw delete allow Samba || true'
ssh root@nighthawk 'ufw allow from 192.168.100.0/24 to any port 10000 proto tcp'
ssh root@nighthawk 'ufw allow from 192.168.100.0/24 to any app Samba'
```

Expected: `10000` と `Samba` はローカルネットワーク (`192.168.100.0/24`) からのみ接続可能になり、インターネット側からは到達不可になる。

**Step 3: SSH hardening**

Run:
```bash
ssh root@nighthawk 'printf "PermitRootLogin prohibit-password\nAllowTcpForwarding no\nX11Forwarding no\n" > /etc/ssh/sshd_config.d/99-hardening.conf && systemctl reload sshd'
```

Expected: root 鍵ログインは維持しつつ攻撃面を縮小。

### Task 9: 最終検証（必須: build -> test -> gwt:compile -> deploy -> 疎通）

**Files:**
- Create: `ops/log/2026-02-21-nighthawk-release-verification.log`

**Step 1: ローカル検証（直列）**

Run:
```bash
mvn -q compile
mvn -q "-Dsurefire.skip=false" test
mvn -q -Pgwt-compile-java25 -DskipTests "-Dgwt.skipCompilation=false" gwt:compile
mvn -q -DskipTests package
```

Expected: すべて成功。

**Step 2: 本番疎通確認**

Run:
```bash
ssh root@nighthawk 'curl -s -o /dev/null -w "%{http_code}\n" https://kishibe.dyndns.tv/QMAClone-1.0-SNAPSHOT/'
ssh root@nighthawk 'curl -s -o /dev/null -w "%{http_code}\n" https://kishibe.dyndns.tv/QMAClone-1.0-SNAPSHOT/tv.dyndns.kishibe.qmaclone.QMAClone/service'
ssh root@nighthawk 'curl -i -N -s --http1.1 -H "Connection: Upgrade" -H "Upgrade: websocket" -H "Sec-WebSocket-Version: 13" -H "Sec-WebSocket-Key: SGVsbG9Xb3JsZDEyMw==" https://kishibe.dyndns.tv/QMAClone-1.0-SNAPSHOT/websocket/tv.dyndns.kishibe.qmaclone.client.packet.PacketServerStatus | head -n 20'
```

Expected:
- `/QMAClone-1.0-SNAPSHOT/` -> `HTTP 200`
- `/service` -> `HTTP 405`
- WebSocket ハンドシェイク -> `101 Switching Protocols`（またはアプリ仕様上の許容コード）

**Step 3: コミット**

```bash
git add ops/config/live/nginx/sites-enabled/default ops/notes ops/log docs/plans/2026-02-21-nighthawk-noble-tomcat10-release-implementation-plan.md
git commit -m "docs: nighthawk本番移行とセキュリティ是正の実行計画を確定"
```
