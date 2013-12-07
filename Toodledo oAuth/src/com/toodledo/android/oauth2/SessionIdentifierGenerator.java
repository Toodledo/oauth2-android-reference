package com.toodledo.android.oauth2;

import java.security.SecureRandom;
import java.math.BigInteger;

public final class SessionIdentifierGenerator {

	private static SecureRandom random = new SecureRandom();

	public static String nextSessionId() {
		return new BigInteger(130, random).toString(32);
	}

}