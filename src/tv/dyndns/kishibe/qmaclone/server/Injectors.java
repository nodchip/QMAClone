package tv.dyndns.kishibe.qmaclone.server;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class Injectors {

  private static final Injector INJECTOR = Guice.createInjector(new QMACloneModule());

  private Injectors() {
    throw new UnsupportedOperationException();
  }

  public static Injector get() {
    return INJECTOR;
  }

}
