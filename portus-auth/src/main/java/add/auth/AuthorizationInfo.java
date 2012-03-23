package add.auth;

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
                    "add.auth.AuthoirzationInfo [options] <code> <url>", null,
                    options, "<code> カスタマコード\n<url> 認可を行うファイルのURL");
            System.exit(1);
        }
    }
}
