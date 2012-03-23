import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Date;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;

public class AuthorizationInfo {
    private static final int STURUCT_REVISION = 1;

    private byte[] code;
    private byte[] url;
    private String ipAddress;
    private Date expire;
    private int count;

    public AuthorizationInfo(String code, String url, String ipAddress,
            Date expire, int count) {
        this.code = code != null ? fromHex(code) : null;
        this.url = url != null ? digestUrl(url.toLowerCase()) : null;
        this.ipAddress = ipAddress;
        this.expire = expire;
        this.count = count;
    }

    public byte[] getCode() {
        return code;
    }

    public byte[] getUrl() {
        return url;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public Date getExpire() {
        return expire;
    }

    public int getCount() {
        return count;
    }

    public String toEncryptedString() throws IOException {
        ByteArrayOutputStream byteout = new ByteArrayOutputStream();
        serialize(new DataOutputStream(byteout));
        return encryptBytes(byteout.toByteArray());
    }

    private void writeBytes(DataOutputStream out, byte[] data)
            throws IOException {
        if (data != null) {
            out.writeShort(data.length);
            out.write(data);
        } else {
            out.writeShort(-1);
        }
    }

    private void serialize(DataOutputStream out) throws IOException {
        out.writeInt(STURUCT_REVISION);
        writeBytes(out, code);
        writeBytes(out, url);
        if (ipAddress != null) {
            out.writeUTF(ipAddress);
        } else {
            out.writeUTF("");
        }
        if (expire != null) {
            out.writeLong(expire.getTime());
        } else {
            out.writeLong(-1L);
        }
        out.writeInt(count);
        out.flush();
    }

    private static byte[] fromHex(String hexdata) {
        if (hexdata == null) {
            return null;
        }

        if (hexdata.length() % 2 != 0) {
            hexdata = "0" + hexdata;
        }

        int len = hexdata.length() / 2;
        byte[] buf = new byte[len];
        for (int i = 0; i < len; ++i) {
            int d = Integer.parseInt(hexdata.substring(i * 2, i * 2 + 2), 16);
            if (d > 127) {
                buf[i] = (byte) d;
            } else {
                buf[i] = (byte) (d - 256);
            }
        }
        return buf;
    }

    private static final Base64 URL_BASE64_INSTANCE = new Base64(
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789._",
            '-');

    private static String toURLFormedBase64(byte[] data) {
        return URL_BASE64_INSTANCE.encode(data);
    }

    private static byte[] fromURLFormedBase64(String code) {
        return URL_BASE64_INSTANCE.decode(code);
    }

    private static final byte[] ASYMMETRIC_ENCRYPTION_KEY_PUBLIC =
            fromURLFormedBase64("MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCmDfJyZEJd0DWNlPAWhNN6X_WNU6y7PovR2P7DMTPmbfZnYdtH1OnMAnmYk3CVxMEo9fbdXUiynXCDiJywQvcgzPyuh224oHN2N6Eb4wo.gnRgrnUF.iiykSscSQwklt9Wl34428q3YhIXK75HJ0E.F6pfUP5q_jQ5v68vMxhFqQIDAQAB");

    private String encryptBytes(byte[] data) {
        try {
            Cipher cipher =
                    ASymmetric.createEncrypter(new ASymmetricKey(false,
                            ASYMMETRIC_ENCRYPTION_KEY_PUBLIC));
            return toURLFormedBase64(cipher.doFinal(data));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static byte[] digestUrl(String url) {
        try {
            MessageDigest md = createDigest();
            return md.digest(url.getBytes(Configuration.INTERNAL_CHARSET));
        } catch (UnsupportedEncodingException e) {
            throw new IllegalConfigurationException(e);
        }
    }

    private static class Configuration {
        public static final String INTERNAL_CHARSET = "UTF-8";

        public static final String DEFAULT_DIGEST = "MD5";

        public static final String DEFAULT_ASYMMETRIC_CIPHER_ALGORITHM = "RSA";
        public static final String DEFAULT_ASYMMETRIC_CIPHER =
                "RSA/ECB/PKCS1Padding";
    }

    public static class IllegalConfigurationException extends RuntimeException {
        private static final long serialVersionUID = -3030667843835148142L;

        public IllegalConfigurationException() {
            super();
        }

        public IllegalConfigurationException(String message, Throwable cause) {
            super(message, cause);
        }

        public IllegalConfigurationException(String message) {
            super(message);
        }

        public IllegalConfigurationException(Throwable cause) {
            super(cause);
        }
    }

    private static class ASymmetricKey {
        private final boolean _private;
        private final byte[] key;

        public ASymmetricKey(boolean privateKey, byte[] key) {
            this._private = privateKey;
            this.key = key;
        }

        public boolean isPrivate() {
            return _private;
        }

        public byte[] getKey() {
            return key;
        }
    }

    private static class ASymmetric {
        public static Cipher createEncrypter(ASymmetricKey key)
                throws InvalidKeyException, InvalidAlgorithmParameterException {
            return createCipher(Cipher.ENCRYPT_MODE, key.isPrivate(),
                    key.getKey());
        }

        private static Cipher createCipher(int mode, boolean isPrivate,
                byte[] key) throws InvalidKeyException,
                InvalidAlgorithmParameterException {
            try {
                KeyFactory factory =
                        KeyFactory.getInstance(Configuration.DEFAULT_ASYMMETRIC_CIPHER_ALGORITHM);

                Key keyspec;
                if (isPrivate) {
                    keyspec =
                            factory.generatePrivate(new PKCS8EncodedKeySpec(key));
                } else {
                    keyspec =
                            factory.generatePublic(new X509EncodedKeySpec(key));
                }

                Cipher cipher =
                        Cipher.getInstance(Configuration.DEFAULT_ASYMMETRIC_CIPHER);
                cipher.init(mode, keyspec);

                return cipher;
            } catch (InvalidKeySpecException e) {
                throw new IllegalConfigurationException(e);
            } catch (NoSuchAlgorithmException e) {
                throw new IllegalConfigurationException(e);
            } catch (NoSuchPaddingException e) {
                throw new IllegalConfigurationException(e);
            }
        }
    }

    private static MessageDigest createDigest() {
        try {
            return MessageDigest.getInstance(Configuration.DEFAULT_DIGEST);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalConfigurationException(e);
        }
    }

    private static class Base64 {
        private final char padding;
        private final char[] encode;
        private final int[] decode;

        public Base64(String code, char padding) {
            this.padding = padding;

            this.encode = code.toCharArray();
            this.decode = new int[128];
            for (int i = 0; i < decode.length; ++i) {
                decode[i] = -1;
            }
            for (int i = 0; i < encode.length; ++i) {
                decode[encode[i]] = i;
            }
        }

        public String encode(byte[] data) {
            StringBuffer encoded = new StringBuffer(data.length * 4 / 3);

            char[] c = new char[4];

            int i = 0;
            while (i < data.length - 2) {
                c[0] = encode[(data[i] & 0xfc) >> 2];
                c[1] =
                        encode[((data[i] & 0x03) << 4)
                                | ((data[++i] & 0xf0) >> 4)];
                c[2] =
                        encode[((data[i] & 0x0f) << 2)
                                | ((data[++i] & 0xc0) >> 6)];
                c[3] = encode[data[i] & 0x3f];

                encoded.append(c);
                i++;
            }

            if (i == data.length - 2) {
                c[0] = encode[(data[i] & 0xfc) >> 2];
                c[1] =
                        encode[((data[i] & 0x03) << 4)
                                | ((data[++i] & 0xf0) >> 4)];
                c[2] = encode[(data[i] & 0x0f) << 2];
                c[3] = padding;
                encoded.append(c);
            } else if (i == data.length - 1) {
                c[0] = encode[(data[i] & 0xfc) >> 2];
                c[1] = encode[(data[i] & 0x03) << 4];
                c[2] = padding;
                c[3] = padding;
                encoded.append(c);
            }

            return encoded.toString();
        }

        public byte[] decode(String code) {
            ByteArrayOutputStream baos =
                    new ByteArrayOutputStream((code.length() * 3 / 4));

            StringReader sr;

            if (code.endsWith(Character.toString(padding))) {
                sr = new StringReader(code.substring(0, code.indexOf(padding)));
            } else {
                sr = new StringReader(code);
            }

            char[] c = new char[4];
            int[] b = new int[4];
            int len;
            try {
                int count = 0;
                while ((len = sr.read(c, 0, 4)) == 4) {
                    b[0] = decode[c[0]];
                    b[1] = decode[c[1]];
                    b[2] = decode[c[2]];
                    b[3] = decode[c[3]];

                    baos.write(((b[0] & 0x3f) << 2) | ((b[1] & 0x30) >> 4));
                    baos.write(((b[1] & 0x0f) << 4) | ((b[2] & 0x3c) >> 2));
                    baos.write(((b[2] & 0x03) << 6) | (b[3] & 0x3f));

                    count++;
                    if (count == 19) {
                        sr.mark(1);
                        count = 0;

                        if (sr.read() != 10) {
                            sr.reset();
                        }
                    }
                }

                b[0] = decode[c[0]];
                b[1] = decode[c[1]];
                b[2] = decode[c[2]];
                b[3] = decode[c[3]];

                if (len == 2) {
                    baos.write(((b[0] & 0x3f) << 2) | ((b[1] & 0x30) >> 4));
                } else if (len == 3) {
                    baos.write(((b[0] & 0x3f) << 2) | ((b[1] & 0x30) >> 4));
                    baos.write(((b[1] & 0x0f) << 4) | ((b[2] & 0x3c) >> 2));
                }

                return baos.toByteArray();
            } catch (java.io.IOException e) {
                return null;
            } catch (RuntimeException e) {
                return null;
            }
        }
    }

    public static void main(String[] args) {
        try {
            String code, url, ipAddress = null;
            Date expire = null;
            switch (args.length) {
            case 4:
                ipAddress = args[3];
            case 3:
                expire =
                        new Date(new Date().getTime()
                                + Integer.parseInt(args[2]) * 1000);
            case 2:
                code = args[0];
                url = args[1];
                break;
            default:
                throw new Exception("パラメータの数が間違っています。");
            }

            AuthorizationInfo info =
                    new AuthorizationInfo(code, url, ipAddress, expire, 1);
            System.out.println(info.toEncryptedString());
        } catch (Exception e) {
            System.err.println("パラメータが間違っています。");
            System.err.println("AuthorizationInfo <code> <url> [ <expire> [ <ip_address> ] ]");
            System.err.println("  <code> カスタマコード");
            System.err.println("  <url>  認可を行うファイルのURL");
            System.err.println("  <expire>  認可コードが有効となる秒数(オプション)");
            System.err.println("  <ip_address> 認可コードが有効となるアクセス元のIPアドレス(オプション)");
            System.exit(1);
        }
    }
}
