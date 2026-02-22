# 2026-02-22 nighthawk セキュリティ是正メモ

## 実施内容
- Ubuntu を `24.04.4 LTS` へ更新。
- Tomcat 実行基盤を `tomcat10` + `Oracle Java 25.0.2` へ移行。
- nginx の TLS/ヘッダー強化を反映。
  - `ssl_protocols TLSv1.2 TLSv1.3`
  - `server_tokens off`
  - `Strict-Transport-Security`
  - `X-Content-Type-Options`
  - `Referrer-Policy`
- SSH hardening を反映。
  - `PermitRootLogin prohibit-password`
  - `AllowTcpForwarding no`
  - `X11Forwarding no`
- `ufw` で LAN 限定公開ポートを適用。
  - `10000/tcp` は `192.168.1.0/24` のみ許可
  - Samba (`137/udp,138/udp,139/tcp,445/tcp`) は `192.168.1.0/24` のみ許可
- 不要サービス公開を停止。
  - `QueenPuzzle-1.0-SNAPSHOT`
  - `TouchTheNumbers-1.0-SNAPSHOT`
  - `zetsubou`
  - `SoundPlayer-1.0-SNAPSHOT`
  - `FourthMission`
  - `DirectorRoom`
  - `LaboratoryC`

## 2026-02-22 時点の公開状態
- `https://kishibe.dyndns.tv/` は Apache (`127.0.0.1:48080`) へリバースプロキシ。
- `https://kishibe.dyndns.tv/qmaclone/` は Apache (`127.0.0.1:48080`) へリバースプロキシ。
- `https://kishibe.dyndns.tv/QMAClone/` は Tomcat (`127.0.0.1:8080/QMAClone/`) へリバースプロキシ。

## 補足
- 本番のゲーム管理画面で「テーマモード編集権限」「制限ユーザー」の表示不具合は解消済み。
- 検証結果の詳細は `ops/log/2026-02-22-nighthawk-release-verification.log` を参照。
