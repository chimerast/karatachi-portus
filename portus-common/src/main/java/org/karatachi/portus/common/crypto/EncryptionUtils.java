package org.karatachi.portus.common.crypto;

import java.io.UnsupportedEncodingException;
import java.nio.charset.UnsupportedCharsetException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;

import org.karatachi.crypto.CipherUtils;
import org.karatachi.exception.IncompatibleConfigurationException;
import org.karatachi.translator.ByteArrayTranslator;

public class EncryptionUtils {

    private static final String INTERNAL_CHARSET = "UTF-8";
    private static final String PASSWORD_DIGEST = "SHA-1";

    public static String passwordDigest(String password) {
        if (password == null) {
            return null;
        }
        try {
            MessageDigest md = MessageDigest.getInstance(PASSWORD_DIGEST);
            return "{sha1}"
                    + ByteArrayTranslator.toBase64(md.digest(password.getBytes(INTERNAL_CHARSET)));
        } catch (UnsupportedEncodingException e) {
            throw new IncompatibleConfigurationException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new IncompatibleConfigurationException(e);
        } catch (UnsupportedCharsetException e) {
            throw new IncompatibleConfigurationException(e);
        }
    }

    public static String toFilePath(long id) {
        StringBuilder ret = new StringBuilder(fileIdToString(id));
        ret.insert(6, '/');
        ret.insert(4, '/');
        ret.insert(2, '/');
        return ret.toString();
    }

    public static String toFilePathWithExtension(long id, String fileName) {
        String path = toFilePath(id);
        int idx = fileName.lastIndexOf(".");
        if (idx == -1) {
            return path;
        } else {
            return path + fileName.substring(idx);
        }
    }

    public static String toFilePathForFlash(long id, String fileName) {
        if (fileName.endsWith(".flv")) {
            return "flv:" + toFilePathWithExtension(id, fileName);
        } else {
            return "mp4:" + toFilePathWithExtension(id, fileName);
        }
    }

    private static byte[] FILE_ID_KEY =
            ByteArrayTranslator.fromBase64("ghnaGGpM8hfJCmzZOAM1Sg==");
    private static byte[] FILE_ID_PARAM =
            ByteArrayTranslator.fromBase64("BBBXUrh1Dc4JERpePZKzL5mB");

    public static String fileIdToString(long l) {
        try {
            byte[] data = longToBytes(l);
            Cipher cipher =
                    CipherUtils.Symmetric.createEncrypter(FILE_ID_KEY,
                            FILE_ID_PARAM);
            return ByteArrayTranslator.toHex(cipher.doFinal(data));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static byte[] CUSTOMER_ID_KEY =
            ByteArrayTranslator.fromBase64("ZA+6/JVdAJSzCiwrKHJlTQ==");
    private static byte[] CUSTOMER_ID_PARAM =
            ByteArrayTranslator.fromBase64("BBC+xByCrNtEzD9vzBtEpBCD");

    public static String customerIdToString(long l) {
        try {
            byte[] data = longToBytes(l);
            Cipher cipher =
                    CipherUtils.Symmetric.createEncrypter(CUSTOMER_ID_KEY,
                            CUSTOMER_ID_PARAM);
            return ByteArrayTranslator.toHex(cipher.doFinal(data));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static long customerIdFromString(String encrypted) {
        try {
            byte[] data = ByteArrayTranslator.fromHex(encrypted);
            Cipher cipher =
                    CipherUtils.Symmetric.createDecrypter(CUSTOMER_ID_KEY,
                            CUSTOMER_ID_PARAM);
            return bytesToLong(cipher.doFinal(data));
        } catch (Exception e) {
            return -1;
        }
    }

    private static byte[] longToBytes(long val) {
        byte[] b = new byte[8];
        b[7] = (byte) (val >>> 0);
        b[6] = (byte) (val >>> 8);
        b[5] = (byte) (val >>> 16);
        b[4] = (byte) (val >>> 24);
        b[3] = (byte) (val >>> 32);
        b[2] = (byte) (val >>> 40);
        b[1] = (byte) (val >>> 48);
        b[0] = (byte) (val >>> 56);
        return b;
    }

    private static long bytesToLong(byte[] b) {
        return ((b[7] & 0xFFL) << 0) + ((b[6] & 0xFFL) << 8)
                + ((b[5] & 0xFFL) << 16) + ((b[4] & 0xFFL) << 24)
                + ((b[3] & 0xFFL) << 32) + ((b[2] & 0xFFL) << 40)
                + ((b[1] & 0xFFL) << 48) + (((long) b[0]) << 56);
    }
}
