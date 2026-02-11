package tv.dyndns.kishibe.qmaclone.server.handwriting;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.util.Random;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import tv.dyndns.kishibe.qmaclone.server.testing.GuiceInjectionExtension;

import com.google.inject.Inject;

@ExtendWith(GuiceInjectionExtension.class)
public class RecognizerZinniaTest {

	@BeforeEach
	public void requireZinniaNative() {
		boolean exists = Files.exists(Paths.get("C:/home/nodchip/zinnia/zinnia/zinnia.dll"));
		assumeTrue(exists, "zinnia.dll が見つからないためスキップ");
	}

	@Inject
	private RecognizerZinnia recognizer;

	@Test
	public final void testRecognize() {
		double[][][] strokes = { { { 51, 29 }, { 117, 41 } }, { { 99, 65 }, { 219, 77 } },
				{ { 27, 131 }, { 261, 131 } }, { { 129, 17 }, { 57, 203 } },
				{ { 111, 71 }, { 219, 173 } }, { { 81, 161 }, { 93, 281 } },
				{ { 99, 167 }, { 207, 167 }, { 189, 245 } }, { { 99, 227 }, { 189, 227 } },
				{ { 111, 257 }, { 189, 245 } }, };
		for (double[][] stroke : strokes) {
			for (double[] p : stroke) {
				for (int i = 0; i < p.length; ++i) {
					p[i] /= 300.0;
				}
			}
		}

		String[] values = recognizer.recognize(strokes);
		assertNotNull(values);
		assertEquals(12, values.length);
		assertEquals("春", values[0]);
	}

	@Test
	public final void testRandomTest() {
		Random r = new Random(0);
		for (int loop = 0; loop < 10; ++loop) {
			int numberOfStrokes = r.nextInt(20) + 1;
			double[][][] strokes = new double[numberOfStrokes][][];
			for (int strokeIndex = 0; strokeIndex < numberOfStrokes; ++strokeIndex) {
				int numberOfPoints = r.nextInt(10) + 2;
				strokes[strokeIndex] = new double[numberOfPoints][];
				for (int pointIndex = 0; pointIndex < numberOfPoints; ++pointIndex) {
					strokes[strokeIndex][pointIndex] = new double[2];
					for (int dimension = 0; dimension < 2; ++dimension) {
						strokes[strokeIndex][pointIndex][dimension] = r.nextDouble();
					}
				}
			}

			String[] values = recognizer.recognize(strokes);
			assertNotNull(values);
			assertEquals(12, values.length);
		}
	}

	@Test
	public final void testGetAvailableCharacters() {
		String availableCharacters = recognizer.getAvailableCharacters();
		assertNotNull(availableCharacters);
		assertEquals(6420, availableCharacters.length());
		assertTrue(availableCharacters.contains("春"));
		assertTrue(availableCharacters.contains("夏"));
		assertTrue(availableCharacters.contains("秋"));
		assertTrue(availableCharacters.contains("冬"));
	}
}
