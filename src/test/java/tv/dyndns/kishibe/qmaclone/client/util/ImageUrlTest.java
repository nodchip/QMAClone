package tv.dyndns.kishibe.qmaclone.client.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class ImageUrlTest {
  @Test
  public void normalize() {
    assertEquals("https://upload.wikimedia.org/wikipedia/commons/7/7e/ImperioOtomano1683.png",
        ImageUrl.normalize(
            "http://upload.wikimedia.org/wikipedia/commons/7/7e/ImperioOtomano1683.png"));
  }
}
