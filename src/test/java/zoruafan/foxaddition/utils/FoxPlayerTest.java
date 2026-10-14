package zoruafan.foxaddition.utils;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.bukkit.util.Vector;
import org.junit.jupiter.api.Test;

class FoxPlayerTest {

	private static final long NEVER = Long.MAX_VALUE;

	@Test
	void walkingAndStaleVelocityAreNotExternal() {
		// Values seen on Paper and Spigot 1.21.11 with a bot standing, walking and after a sprint-jump.
		assertFalse(FoxPlayer.hasExternalVelocity(new Vector(0.0, -0.0784, 0.0), NEVER));
		assertFalse(FoxPlayer.hasExternalVelocity(new Vector(0.0689, -0.0784, 0.0165), NEVER));
		assertFalse(FoxPlayer.hasExternalVelocity(new Vector(-0.1237, -0.1523, -0.0297), NEVER));
		assertFalse(FoxPlayer.hasExternalVelocity(new Vector(0.6462, 0.0, 0.0001), NEVER));
	}

	@Test
	void recentServerVelocityCounts() {
		assertTrue(FoxPlayer.hasExternalVelocity(new Vector(0.0, -0.0784, 0.0), 200));
		assertTrue(FoxPlayer.hasExternalVelocity(new Vector(-0.4, -0.0784, 0.0), 1499));
		assertFalse(FoxPlayer.hasExternalVelocity(new Vector(-0.4, -0.0784, 0.0), 1500));
	}

	@Test
	void upwardMovementCounts() {
		assertTrue(FoxPlayer.hasExternalVelocity(new Vector(0.1817, 0.1648, 0.0436), NEVER));
	}
}
