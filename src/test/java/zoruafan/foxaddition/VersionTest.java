package zoruafan.foxaddition;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class VersionTest {

	@Test
	void olderThan17OnlyForOldOneDotVersions() {
		assertTrue(FoxAddition.isOlderThan17("1.6.4-R2.0"));
		assertFalse(FoxAddition.isOlderThan17("1.7.10-R0.1-SNAPSHOT"));
		assertFalse(FoxAddition.isOlderThan17("1.8.8-R0.1-SNAPSHOT"));
		assertFalse(FoxAddition.isOlderThan17("1.21.11-R0.1-SNAPSHOT"));
	}

	@Test
	void yearBasedVersionsAreNotOld() {
		// Paper 26.2 reports this; it used to be read as 1.2.
		assertFalse(FoxAddition.isOlderThan17("26.2.build.134-stable"));
		assertFalse(FoxAddition.isOlderThan17("26.1.2-R0.1-SNAPSHOT"));
	}

	@Test
	void compareVersions() {
		assertTrue(FoxAdditionAPI.compareVersions("1.2.3-beta.2", "1.2.2") > 0);
		assertTrue(FoxAdditionAPI.compareVersions("1.2.3-beta.2", "1.2.3") < 0);
		assertTrue(FoxAdditionAPI.compareVersions("1.2.2", "1.2.3-beta.2") < 0);
		assertTrue(FoxAdditionAPI.compareVersions("1.10", "1.9") > 0);
		assertEquals(0, FoxAdditionAPI.compareVersions("1.2.3", "1.2.3"));
		assertEquals(0, FoxAdditionAPI.compareVersions("1.2", "1.2.0"));
	}
}
