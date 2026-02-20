# ops/config ディレクトリ運用

このディレクトリは、自宅サーバーの実運用設定ファイルをローカル保管するための置き場です。
`live` 配下に生ファイルを配置し、Git には含めません。

## 構成
- `ops/config/live/nginx/`: nginx 設定ファイル
- `ops/config/live/nginx/sites-enabled/`: nginx 仮想ホスト設定
- `ops/config/live/tomcat9/`: tomcat9 設定ファイル

## 配置例
- `ops/config/live/nginx/nginx.conf`
- `ops/config/live/nginx/sites-enabled/kishibe.dyndns.tv.conf`
- `ops/config/live/tomcat9/server.xml`
- `ops/config/live/tomcat9/web.xml`
- `ops/config/live/tomcat9/qmaclone-admin.properties`

`qmaclone-admin.properties` には管理者判定の設定を記述します。

```properties
admin.enforcement.enabled=true
admin.google.sub.allowlist=sub1,sub2
```

## Git 方針
- `ops/config/live/**` は `.gitignore` で除外します。
- 事故防止のため、設定ファイルを `git add -f` で強制追加しないでください。
- 必要な共有情報は、設定ファイル本体ではなく本 README に追記してください。
