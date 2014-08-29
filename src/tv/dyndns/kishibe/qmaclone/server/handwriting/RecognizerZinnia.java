package tv.dyndns.kishibe.qmaclone.server.handwriting;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;

import com.google.common.base.Joiner;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.sun.jna.Pointer;

public class RecognizerZinnia implements Recognizable {

  private static final int SIZE = 300;
  private static final Object STATIC_KEY = new Object();
  private static final Logger logger = Logger.getLogger(RecognizerZinnia.class.toString());
  private final LoadingCache<Object, String> availableCharacters = CacheBuilder.newBuilder()
      .concurrencyLevel(1).build(new CacheLoader<Object, String>() {
        @Override
        public String load(Object arg0) throws Exception {
          return loadAvailableCharacters();
        }
      });
  // recognizerのインスタンスはObjectPoolに保持させる
  private final ZinniaLibrary zinnia;
  private final ObjectPool<Pointer> pool;

  @Inject
  public RecognizerZinnia(ZinniaLibrary zinnia, ZinniaObjectFactory zinniaObjectFactory) {
    this.zinnia = zinnia;
    this.pool = new GenericObjectPool<Pointer>(zinniaObjectFactory);
  }

  private String loadAvailableCharacters() throws NoSuchElementException, IllegalStateException,
      Exception {
    Pointer recognizer = null;
    try {
      recognizer = (Pointer) pool.borrowObject();
      int sizeOfRecognizer = (int) zinnia.zinnia_recognizer_size(recognizer);
      Set<String> characters = Sets.newTreeSet();
      for (int i = 0; i < sizeOfRecognizer; ++i) {
        characters.add(zinnia.zinnia_recognizer_value(recognizer, i));
      }
      return Joiner.on("").join(characters);
    } finally {
      if (recognizer != null) {
        pool.returnObject(recognizer);
      }
    }

  }

  @Override
  public String[] recognize(double[][][] inputStrokes) {
    Pointer character = zinnia.zinnia_character_new();
    zinnia.zinnia_character_clear(character);
    zinnia.zinnia_character_set_width(character, SIZE);
    zinnia.zinnia_character_set_height(character, SIZE);
    int strokeIndex = 0;
    for (double[][] stroke : inputStrokes) {
      for (double[] p : stroke) {
        int x = (int) (p[0] * SIZE + 0.5);
        int y = (int) (p[1] * SIZE + 0.5);
        zinnia.zinnia_character_add(character, strokeIndex, x, y);
      }
      ++strokeIndex;
    }

    Pointer recognizer = null;
    Pointer result = null;
    try {
      recognizer = (Pointer) pool.borrowObject();

      result = zinnia.zinnia_recognizer_classify(recognizer, character, 12);

      if (result == null) {
        System.err.printf("%s\n", zinnia.zinnia_recognizer_strerror(recognizer));
        return null;
      }

      List<String> values = new ArrayList<String>();
      for (int i = 0; i < zinnia.zinnia_result_size(result); ++i) {
        String value = zinnia.zinnia_result_value(result, i);
        values.add(value);
      }

      return values.toArray(new String[0]);

    } catch (Exception e) {
      logger.log(Level.WARNING, "文字の認識に失敗しました", e);

    } finally {
      try {
        if (recognizer != null) {
          pool.returnObject(recognizer);
          recognizer = null;
        }
      } catch (Exception e) {
        e.printStackTrace();
      }

      if (result != null) {
        zinnia.zinnia_result_destroy(result);
      }

      if (character != null) {
        zinnia.zinnia_character_destroy(character);
      }
    }

    return null;
  }

  @Override
  public String getAvailableCharacters() {
    try {
      return availableCharacters.get(STATIC_KEY);
    } catch (ExecutionException e) {
      logger.log(Level.WARNING, "文字認識エンジンで使用可能な文字の取得に失敗しました");
      return null;
    }
  }

}
