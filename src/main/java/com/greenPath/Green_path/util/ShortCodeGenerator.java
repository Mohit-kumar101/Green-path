package com.greenPath.Green_path.util;

import java.security.SecureRandom;

public final class ShortCodeGenerator {

	private static final char[] ALPHABET =
			"abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray();
	private static final SecureRandom RANDOM = new SecureRandom();

	private ShortCodeGenerator() {
	}

	public static String randomCode(int length) {
		char[] buf = new char[length];
		for (int i = 0; i < length; i++) {
			buf[i] = ALPHABET[RANDOM.nextInt(ALPHABET.length)];
		}
		return new String(buf);
	}
}
