package org.karatachi.portus.common.net;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.util.Date;

import javax.crypto.Cipher;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.karatachi.crypto.CipherUtils;
import org.karatachi.crypto.CipherUtils.ASymmetricKey;
import org.karatachi.crypto.DigestUtils;
import org.karatachi.translator.ByteArrayTranslator;
import org.msgpack.Packer;
import org.msgpack.Unpacker;

public class AuthorizationInfo {
    public static final int STRUCTURE_REVISION = 1;

    public byte[] code;
    public byte[] url;
    public String ipAddress;
    public long expire;
    public String username;

    public AuthorizationInfo() {
    }

    public AuthorizationInfo(String code, String url, String ipAddress,
            Date expire, String username) {
        this.code = ByteArrayTranslator.fromHex(code);
        this.url = digestUrl(url.toLowerCase());
        this.ipAddress = ipAddress != null ? ipAddress : "0.0.0.0";
        this.expire = expire != null ? expire.getTime() : -1L;
        this.username = username != null ? username : "";
    }

    @Override
    public String toString() {
        return encrypt(this);
    }

    private static byte[] pack(AuthorizationInfo info) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Packer packer = new Packer(out);
        packer.pack(STRUCTURE_REVISION);
        packer.pack(info.code);
        packer.pack(info.url);
        packer.pack(info.ipAddress);
        packer.pack(info.expire);
        packer.pack(info.username);
        return out.toByteArray();
    }

    private static AuthorizationInfo unpack(byte[] buf) throws IOException {
        ByteArrayInputStream in = new ByteArrayInputStream(buf);
        Unpacker unpacker = new Unpacker(in);
        AuthorizationInfo info = new AuthorizationInfo();
        unpacker.unpackInt();
        info.code = unpacker.unpackByteArray();
        info.url = unpacker.unpackByteArray();
        info.ipAddress = unpacker.unpackString();
        info.expire = unpacker.unpackLong();
        info.username = unpacker.unpackString();
        return info;
    }

    private static final byte[] ASYMMETRIC_ENCRYPTION_KEY_PRIVATE =
            ByteArrayTranslator.fromBase64("MIICeAIBADANBgkqhkiG9w0BAQEFAASCAmIwggJeAgEAAoGBAIa2pPzYDV/v3BHWSEcKgBMXtrnAWXnR2wW9hDp25QwzgQbsrMIFpd8WuErwZjxKmEdoAZxRqq5j7UlAWQpCsBex0UutflgU7cOFuQjo9yHFZVlDJM40vujLG9DkGXDzv0pJToSeI/dpBqPUI1ejwaaXqq3CaNAqjPeadfZxCV2xAgMBAAECgYBGUmOwBYx9zzU/Lm/OfeG7lb5yGsHagLznszWqW1RX2S76kWPhaJdc7HxJylJkGMCDTfzBQDRsEofUnD2eyI8pGtjg4LTqlKOmhlhBNaGfBGxScQBXFvS8YYGKMJKgOuuj4L9aARnznG4yJgONBOB5QF+yHf5dOy25m38OoYdE3QJBANqNrA1u/bRZk/NvwvzYc2slcyV2AY5mL/jDca7RjbQX1LIULOU/Pd+9nsGwe2sOfEfhz3BG5An/OHvT2RNjrVsCQQCdy4gG7CammlAq1AptEz/tDEwdKwndo7qQOesho67L4OharIjhycnHaQroPzhRl49D/etGs9Dz0iCol4SS9tLjAkEAjYXL8e77bnvLKIook55+LtXTWGCv5UwaqFW3GeMshYAhBSe3YsOvB2E8mmPzp4F7zPhWYXgmVqrkIpsuKDYVnQJBAIlWyclEpwtgyh/MuFphY8Vda64DdK3NQKchUg7QWWYoFfQ9sVHcANyoB49G3yzc3hwOmEXMYjSlEjnKzt/fJoMCQQDO7RYkzV9lWgge6n5t6cgpWUoRzTWmRTdfjQ31zePobXdF8iVHT6OUKpb8b3EWsDjzcGmiyKl9S+FYxq4Bqm6N");
    private static final byte[] ASYMMETRIC_ENCRYPTION_KEY_PUBLIC =
            ByteArrayTranslator.fromBase64("MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCGtqT82A1f79wR1khHCoATF7a5wFl50dsFvYQ6duUMM4EG7KzCBaXfFrhK8GY8SphHaAGcUaquY+1JQFkKQrAXsdFLrX5YFO3DhbkI6PchxWVZQyTONL7oyxvQ5Blw879KSU6EniP3aQaj1CNXo8Gml6qtwmjQKoz3mnX2cQldsQIDAQAB");

    public static String encrypt(AuthorizationInfo authorizationInfo) {
        try {
            Cipher cipher =
                    CipherUtils.ASymmetric.createEncrypter(new ASymmetricKey(
                            false, ASYMMETRIC_ENCRYPTION_KEY_PUBLIC));
            return ByteArrayTranslator.toURLFormedBase64(cipher.doFinal(pack(authorizationInfo)));
        } catch (Exception e) {
            return null;
        }
    }

    public static AuthorizationInfo decrypt(String encrypted) {
        try {
            byte[] data = ByteArrayTranslator.fromURLFormedBase64(encrypted);
            Cipher cipher =
                    CipherUtils.ASymmetric.createDecrypter(new ASymmetricKey(
                            true, ASYMMETRIC_ENCRYPTION_KEY_PRIVATE));
            return unpack(cipher.doFinal(data));
        } catch (Exception e) {
            return null;
        }
    }

    public static byte[] digestUrl(String url) {
        try {
            MessageDigest md = DigestUtils.createDigest();
            return md.digest(url.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }

    @SuppressWarnings("static-access")
    public static void main(String[] args) {
        CommandLineParser parser = new GnuParser();

        Options options = new Options();
        options.addOption(OptionBuilder.withLongOpt("expire").withDescription(
                "認可コードが有効となる秒数").hasArg().withArgName("seconds").create("e"));
        options.addOption(OptionBuilder.withLongOpt("ipaddress").withDescription(
                "認可コードが有効となるアクセス元のIPアドレス").hasArg().withArgName("ipaddress").create(
                "i"));
        options.addOption(OptionBuilder.withLongOpt("username").withDescription(
                "ログに記録するユーザー名").hasArg().withArgName("usernmae").create("u"));

        try {
            CommandLine cmd = parser.parse(options, args);

            String[] commands = cmd.getArgs();
            if (commands.length != 2) {
                throw new ParseException("引数の数は<code>と<url>の２つです。");
            }

            String code = commands[0];
            String url = commands[1];

            Date expire = null;
            if (cmd.hasOption("e")) {
                try {
                    expire =
                            new Date(new Date().getTime()
                                    + Integer.parseInt(cmd.getOptionValue("e"))
                                    * 1000);
                } catch (NumberFormatException e) {
                }
            }

            String ipAddress = cmd.getOptionValue("i", null);
            String username = cmd.getOptionValue("u", null);

            AuthorizationInfo info =
                    new AuthorizationInfo(code, url, ipAddress, expire,
                            username);
            System.out.println(AuthorizationInfo.encrypt(info));
        } catch (Exception e) {
            System.err.println("error: " + e.getMessage());
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp(
                    "org.karatachi.portus.AuthoirzationInfo [options] <code> <url>",
                    null, options, "<code> カスタマコード\n<url> 認可を行うファイルのURL");
            System.exit(1);
        }
    }
}
