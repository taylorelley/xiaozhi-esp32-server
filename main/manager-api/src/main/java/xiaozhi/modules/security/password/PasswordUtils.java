package xiaozhi.modules.security.password;

/**
 * Passwordtoolclass
 * Copyright (c) Renren Opensource All rights reserved.
 * Website: https://www.renren.io
 */
public class PasswordUtils {
    private static PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * encrypt
     *
     * @param str string
     * @return returnencryptstring
     */
    public static String encode(String str) {
        return passwordEncoder.encode(str);
    }

    /**
     * 比较PasswordYesNo相etc.
     *
     * @param str      明文Password
     * @param password encryptafterPassword
     * @return true：success false：failed
     */
    public static boolean matches(String str, String password) {
        return passwordEncoder.matches(str, password);
    }

    public static void main(String[] args) {
        String str = "admin";
        String password = encode(str);

        System.out.println(password);
        System.out.println(matches(str, password));
    }

}
