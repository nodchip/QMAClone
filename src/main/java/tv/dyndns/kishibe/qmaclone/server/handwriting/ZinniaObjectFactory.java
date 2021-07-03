package tv.dyndns.kishibe.qmaclone.server.handwriting;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

import com.google.inject.Inject;
import com.sun.jna.Pointer;

import tv.dyndns.kishibe.qmaclone.client.constant.Constant;

public class ZinniaObjectFactory extends BasePooledObjectFactory<Pointer> {
  private static final String MODEL_FILE_PATH = Constant.FILE_PATH_BASE + "handwriting-ja.model";
  private final ZinniaLibrary zinnia;

  @Inject
  public ZinniaObjectFactory(ZinniaLibrary zinnia) {
    this.zinnia = zinnia;
  }

  @Override
  public Pointer create() throws Exception {
    Pointer recognizer = zinnia.zinnia_recognizer_new();

    if (zinnia.zinnia_recognizer_open(recognizer, MODEL_FILE_PATH) == 0) {
      System.err.printf("ERROR: %s\n", zinnia.zinnia_recognizer_strerror(recognizer));
      zinnia.zinnia_recognizer_destroy(recognizer);
      throw new Exception("zinnia 辞書ファイルの読み込みに失敗しました");
    }

    return recognizer;
  }

  @Override
  public PooledObject<Pointer> wrap(Pointer obj) {
    return new DefaultPooledObject<Pointer>(obj);
  }

  @Override
  public void destroyObject(PooledObject<Pointer> p) throws Exception {
    Pointer recognizer = (Pointer) p.getObject();
    zinnia.zinnia_recognizer_destroy(recognizer);
  }
}
