package tv.dyndns.kishibe.qmaclone.server.testing;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;

import com.google.inject.Guice;
import com.google.inject.Injector;

import tv.dyndns.kishibe.qmaclone.server.QMACloneModule;

/**
 * Jupiter テストインスタンスへ Guice のメンバー注入を行う拡張。
 */
public class GuiceInjectionExtension implements TestInstancePostProcessor {

  /**
   * テストインスタンス生成後に Guice 注入を適用する。
   *
   * @param testInstance 注入対象のテストインスタンス
   * @param context 実行コンテキスト
   */
  @Override
  public void postProcessTestInstance(Object testInstance, ExtensionContext context) {
    Injector injector = getOrCreateInjector(context);
    injector.injectMembers(testInstance);
  }

  /**
   * テストクラス単位で Injector を生成または取得する。
   *
   * @param context 実行コンテキスト
   * @return 利用可能な Injector
   */
  private Injector getOrCreateInjector(ExtensionContext context) {
    ExtensionContext.Namespace namespace =
        ExtensionContext.Namespace.create(GuiceInjectionExtension.class, context.getRequiredTestClass());
    ExtensionContext.Store store = context.getRoot().getStore(namespace);
    return store.getOrComputeIfAbsent(
        Injector.class, key -> Guice.createInjector(new QMACloneModule()), Injector.class);
  }
}
