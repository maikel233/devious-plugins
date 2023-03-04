package net.runelite.client.plugins.xAutologin;

import org.mindrot.jbcrypt.BCrypt;

public class xAutoLoginPassword {
    private static final int WORKLOAD = 12;

    public static String hashPassword(String password) {
        String salt = BCrypt.gensalt(WORKLOAD);
        return BCrypt.hashpw(password, salt);
    }

    public static boolean checkPassword(String password, String hashedPassword) {
        return BCrypt.checkpw(password, hashedPassword);
    }
}