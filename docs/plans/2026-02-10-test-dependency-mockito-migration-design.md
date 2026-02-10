# Test Dependency Mockito Migration Design

## 1. Goal
- test依存のうち `mockito-all` を最小リスクで更新し、既存テストの実行互換を維持する。
- runtime依存や機能コードには影響を広げない。

## 2. Scope
### In Scope
- `pom.xml` の `mockito-all` を `mockito-core` へ置換
- 置換で発生した最小限の test import 修正
- 既存テストの回帰確認（対象テスト + compile/package）

### Out of Scope
- `junit`, `truth`, `hamcrest` など他の test依存更新
- テストロジック変更、仕様変更
- runtime依存の追加更新

## 3. Strategy
- 方針は「最小リスク・最小差分」。
- 依存変更はまず `mockito-all` のみ。
- テストコードは原則無変更とし、ビルドエラー時のみ import やAPI差分の最小修正を許可。

## 4. Implementation Flow
1. `pom.xml` の `org.mockito:mockito-all` を削除
2. `org.mockito:mockito-core` を追加（安定版）
3. `mvn -Dtest=PanelSettingUserCodePresenterTest test` 実行
4. 必要時のみ `MockitoJUnitRunner` import 差し替え等の最小修正
5. `mvn -DskipTests compile` と `mvn package -DskipTests` 実行

## 5. Verification
- 必須コマンド
  - `mvn -Dtest=PanelSettingUserCodePresenterTest test`
  - `mvn -DskipTests compile`
  - `mvn package -DskipTests`
- 期待結果
  - BUILD SUCCESS
  - 対象テスト Failures=0 / Errors=0

## 6. Risk & Rollback
### Main Risks
- `org.mockito.runners.MockitoJUnitRunner` から `org.mockito.junit.MockitoJUnitRunner` への差分
- `Matchers` 系APIの差分によるコンパイル失敗

### Mitigation
- 修正は import と最小API置換に限定
- テスト意味を変える修正は行わない

### Rollback
- `mockito-core` 置換で互換が取れない場合、`mockito-all` に即時戻す
- 失敗ログを記録し、次フェーズでバージョン戦略を再検討

## 7. Done Criteria
- `mockito-all` が `pom.xml` から消えている
- `mockito-core` への置換が完了
- 3つの検証コマンドが成功
- 変更差分が test依存 + 最小test修正に限定されている