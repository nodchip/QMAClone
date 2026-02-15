# 12. ClientBundle

対象: ClientBundle
- https://www.gwtproject.org/doc/latest/DevGuideClientBundle.html

## 要点
- 画像・CSSなど静的資産を型安全に束ね、最適化しやすくする。
- リソース参照の一元化で、ファイル名変更や最適化時の破壊を減らせる。

## QMAClone適用
- アイコンやテーマ資産の再利用箇所が多い画面で有効。
- 現状の`webapp`直参照資産を段階的に移行する場合の基礎として使う。

## 関連
- CSS: [06-css-styling.md](06-css-styling.md)
- 最適化: [11-code-splitting.md](11-code-splitting.md)
