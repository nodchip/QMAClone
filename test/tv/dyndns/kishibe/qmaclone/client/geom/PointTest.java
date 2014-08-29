package tv.dyndns.kishibe.qmaclone.client.geom;

import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class PointTest {
	@Test
	public final void testHashCode() {
		Point p;

		int hashCode1 = new Point(2, 3).hashCode();
		int hashCode2 = new Point(123, 234).hashCode();
		assertThat(hashCode1, not(hashCode2));
	}

	@Test
	public final void testPointIntInt() {
		Point p;
		p = new Point(3, 4);
		assertEquals(3, p.x);
		assertEquals(4, p.y);

		p = new Point(234, 345);
		assertEquals(234, p.x);
		assertEquals(345, p.y);
	}

	@Test
	public final void testPointPoint() {
		Point p;

		p = new Point(4, 5);
		assertEquals(4, p.x);
		assertEquals(5, p.y);

		p = new Point(345, 456);
		assertEquals(345, p.x);
		assertEquals(456, p.y);
	}

	@Test
	public final void testParse() {
		Point p;

		p = Point.fromString("5 6");
		assertNotNull(p);
		assertEquals(5, p.x);
		assertEquals(6, p.y);

		p = Point.fromString("567 678");
		assertNotNull(p);
		assertEquals(567, p.x);
		assertEquals(678, p.y);

		p = Point.fromString(null);
		assertNull(p);

		p = Point.fromString("");
		assertNull(p);

		p = Point.fromString("7");
		assertNull(p);

		p = Point.fromString("7 8 9");
		assertNull(p);

		p = Point.fromString("000");
		assertNull(p);

		p = Point.fromString("・・・・・・・・");
		assertNull(p);

		p = Point.fromString("犬養毅");
		assertNull(p);
	}

	@Test
	public final void testToString() {
		Point p;

		p = new Point(7, 8);
		assertEquals("7 8", p.toString());

		p = new Point(678, 789);
		assertEquals("678 789", p.toString());
	}

	@Test
	public final void testMinus() {
		Point p;

		p = new Point(10, 11);
		p = p.minus(new Point(2, 2));
		assertEquals(new Point(8, 9), p);
	}

	@Test
	public final void testNorm() {
		Point p;

		p = new Point(10, 11);
		assertEquals(221, p.norm());

		p = new Point(20, 40);
		assertEquals(2000, p.norm());
	}

	@Test
	public final void testEqualsObject() {
		Point p;

		p = new Point(321, 432);
		assertEquals(new Point(321, 432), p);

		p = new Point(3, 2);
		assertEquals(new Point(3, 2), p);
	}

	@Test
	public final void testIsValid() {
		Point p;

		p = new Point(Point.INVALID, Point.INVALID);
		assertFalse(p.isValid());

		p = new Point(432, Point.INVALID);
		assertFalse(p.isValid());

		p = new Point(Point.INVALID, 543);
		assertFalse(p.isValid());

		p = new Point(432, 543);
		assertTrue(p.isValid());
	}
}
