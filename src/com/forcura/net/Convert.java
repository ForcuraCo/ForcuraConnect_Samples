package com.forcura.net;

import java.nio.charset.Charset;
import java.util.Base64;

final class Convert {
    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();

    static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    static String toBase64String(byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes);
    }

    static byte[] fromBase64String(String str) {
        return Base64.getDecoder().decode(str);
    }
    
    static byte[] toBytes(String str) {
        return toBytes(str, "UTF-8");
    }

    static byte[] toBytes(String str, String encoding) {
        return str.getBytes(Charset.forName(encoding));
    }
}
