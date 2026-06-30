package com.drdo.gis.util;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtil {

    private static final int COST = 12;

    public static String hash(String plainText) {
        return BCrypt.hashpw(plainText, BCrypt.gensalt(COST));
    }

    public static boolean verify(String plainText, String hash) {
        if (hash == null || hash.isEmpty() || !hash.startsWith("$2")) {
            return plainText.equals(hash);
        }
        return BCrypt.checkpw(plainText, hash);
    }
}
