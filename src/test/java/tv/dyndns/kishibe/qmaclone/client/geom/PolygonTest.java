package tv.dyndns.kishibe.qmaclone.client.geom;

import org.junit.Before;
import org.junit.Test;

public class PolygonTest {

  @Before
  public void setUp() throws Exception {
  }

  @Test(expected = PolygonException.class)
  public void fromStringThrowsExceptionOnTooManyVerticies() throws Exception {
    String polygon = "0 0 1 0 2 0 3 0 4 0 5 0 6 0 7 0 8 0 9 0 10 0 0 10";

    Polygon.fromString(polygon);
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

  @Test(expected = PolygonException.class)
  public void fromStringThrowsExceptionOn1Vertex() throws Exception {
    String polygon = "0 0";

    Polygon.fromString(polygon);
  }

  @Test(expected = PolygonException.class)
  public void fromStringThrowsExceptionOn2Vertex() throws Exception {
    String polygon = "0 0 1 0";

    Polygon.fromString(polygon);
  }
}
