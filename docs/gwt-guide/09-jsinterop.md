# 09. JsInterop

対象: JsInterop
- https://www.gwtproject.org/doc/latest/DevGuideCodingBasicsJsInterop.html

## 要点
- JSNIよりもJsInteropを優先するのが現在の推奨。
- `@JsType` / `@JsMethod` / `@JsProperty` を使ってJavaとJSの境界を明示する。
- 型境界を曖昧にすると、ランタイム不整合が増える。

## QMAClone適用
- 新規でJavaScript連携が必要な場合はJsInteropを優先。
- 既存JSNIに手を入れる場合は、段階的置換の可否を先に判断する。

## 関連
- Deferred Binding: [10-deferred-binding.md](10-deferred-binding.md)
- 最適化: [11-code-splitting.md](11-code-splitting.md)
