package zoruafan.foxaddition.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

class BrandCheckerTest {

	private static byte[] payload(String brand) {
		byte[] text = brand.getBytes(StandardCharsets.UTF_8);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		int n = text.length;
		do {
			int b = n & 0x7F;
			n >>>= 7;
			out.write(n != 0 ? b | 0x80 : b);
		} while (n != 0);
		out.write(text, 0, text.length);
		return out.toByteArray();
	}

	@Test
	void readsTheLengthPrefixedBrand() {
		assertEquals("vanilla", BrandChecker.readBrand(payload("vanilla")));
		// 40 characters: the length byte is '(' and used to end up in front of the brand.
		String longBrand = "evil-client-with-a-very-long-brand-name1";
		assertEquals(longBrand, BrandChecker.readBrand(payload(longBrand)));
		String veryLong = new String(new char[200]).replace('\0', 'x');
		assertEquals(veryLong, BrandChecker.readBrand(payload(veryLong)));
	}

	@Test
	void fallsBackToTheRawBytesForBrokenPayloads() {
		assertEquals("abc", BrandChecker.readBrand("abc".getBytes(StandardCharsets.UTF_8)));
	}

	@Test
	void sanitizeKeepsCommandsSafe() {
		assertEquals("evil__c_Staff__fake_message__player_name_", BrandChecker.sanitize("evil &c[Staff] fake message %player_name%"));
		assertEquals("lunarclient:1.2.3-abc", BrandChecker.sanitize("lunarclient:1.2.3-abc"));
		assertEquals(64, BrandChecker.sanitize(new String(new char[100]).replace('\0', 'a')).length());
	}

	@Test
	void patternsUseOnlyStarAsWildcardAndIgnoreCase() {
		assertTrue(BrandChecker.matchesPattern("Fabric", "fabric"));
		assertTrue(BrandChecker.matchesPattern("lunarclient:*", "lunarclient:abc123"));
		assertTrue(BrandChecker.matchesPattern("*forge*", "fml,forge"));
		assertFalse(BrandChecker.matchesPattern("vanilla", "vanilla-hacked"));
		// '.' is a literal dot, not "any character".
		assertFalse(BrandChecker.matchesPattern("a.b", "axb"));
		assertTrue(BrandChecker.matchesPattern("a.b", "a.b"));
	}
}
