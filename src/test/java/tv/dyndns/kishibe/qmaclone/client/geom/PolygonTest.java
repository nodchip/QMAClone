package tv.dyndns.kishibe.qmaclone.client.geom;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class PolygonTest {

  @BeforeEach
  public void setUp() throws Exception {
  }

  @Test
  public void fromStringThrowsExceptionOnTooManyVerticies() throws Exception {
    String polygon = "0 0 1 0 2 0 3 0 4 0 5 0 6 0 7 0 8 0 9 0 10 0 0 10";

    assertThrows(PolygonException.class, () -> Polygon.fromString(polygon));
  }

  @Test
  public void fromStringAccepts8Vertices() throws Exception {
    String polygon = "0 0 1 0 2 0 3 0 4 0 5 0 6 0 7 10";

    Polygon.fromString(polygon);
  }

  @Test
  public void fromStringAccepts3Vertices() throws Exception {
    String polygon = "0 0 1 0 2 10";

    Polygon.fromString(polygon);
  }

  @Test
  public void fromStringThrowsExceptionOn1Vertex() throws Exception {
    String polygon = "0 0";

    assertThrows(PolygonException.class, () -> Polygon.fromString(polygon));
  }

  @Test
  public void fromStringThrowsExceptionOn2Vertex() throws Exception {
    String polygon = "0 0 1 0";

    assertThrows(PolygonException.class, () -> Polygon.fromString(polygon));
  }
}
