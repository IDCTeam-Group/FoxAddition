package zoruafan.foxaddition.checks.mechanics;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PositionTest {

	@Test
	void validRotations() {
		assertFalse(Position.isInvalidRotation(0f, 0f));
		assertFalse(Position.isInvalidRotation(-179.9f, 90f));
		assertFalse(Position.isInvalidRotation(720.5f, -90f));
	}

	@Test
	void invalidRotations() {
		assertTrue(Position.isInvalidRotation(0f, 90.5f));
		assertTrue(Position.isInvalidRotation(0f, -1000f));
		assertTrue(Position.isInvalidRotation(0f, Float.NaN));
		assertTrue(Position.isInvalidRotation(Float.NaN, 0f));
		assertTrue(Position.isInvalidRotation(Float.POSITIVE_INFINITY, 0f));
	}
}
