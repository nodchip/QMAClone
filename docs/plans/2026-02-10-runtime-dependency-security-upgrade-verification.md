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