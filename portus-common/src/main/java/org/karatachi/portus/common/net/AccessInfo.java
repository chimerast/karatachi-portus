package org.karatachi.portus.common.net;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.crypto.Cipher;

import org.karatachi.crypto.CipherUtils;
import org.karatachi.translator.ByteArrayTranslator;
import org.msgpack.Packer;
import org.msgpack.Unpacker;

public class AccessInfo {
    public static final int STRUCTURE_REVISION = 1;

    public long fileId;
    public String ipAddress;
    public String fileName;
    public long expire;
    public boolean nocache;
    public boolean countup;

    public AccessInfo() {
    }

    public AccessInfo(long fileId, String ipAddress, String fileName,
            long expire, boolean nocache, boolean countup) {
        this.fileId = fileId;
        this.ipAddress = ipAddress;
        this.fileName = fileName;
        this.expire = expire;
        this.nocache = nocache;
        this.countup = countup;
    }

    @Override
    public String toString() {
        return encrypt(this);
    }

    private static byte[] pack(AccessInfo info) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Packer packer = new Packer(out);
        packer.pack(STRUCTURE_REVISION);
        packer.pack(info.fileId);
        packer.pack(info.ipAddress);
        packer.pack(info.fileName);
        packer.pack(info.expire);
        packer.pack(info.nocache);
        packer.pack(info.countup);
        return out.toByteArray();
    }

    private static AccessInfo unpack(byte[] buf) throws IOException {
        ByteArrayInputStream in = new ByteArrayInputStream(buf);
        Unpacker unpacker = new Unpacker(in);
        AccessInfo info = new AccessInfo();
        unpacker.unpackInt();
        info.fileId = unpacker.unpackLong();
        info.ipAddress = unpacker.unpackString();
        info.fileName = unpacker.unpackString();
        info.expire = unpacker.unpackLong();
        info.nocache = unpacker.unpackBoolean();
        info.countup = unpacker.unpackBoolean();
        return info;
    }

    private static final byte[] ACCESS_INFO_KEY =
            ByteArrayTranslator.fromBase64("iw1moDpy/HNCTrRfkebxPw==");
    private static final byte[] ACCESS_INFO_PARAM =
            ByteArrayTranslator.fromBase64("BBAXmQc0l+TrIV0XE32XH6Fm");

    public static String encrypt(AccessInfo accessInfo) {
        try {
            Cipher cipher =
                    CipherUtils.Symmetric.createEncrypter(ACCESS_INFO_KEY,
                            ACCESS_INFO_PARAM);
            return ByteArrayTranslator.toURLFormedBase64(cipher.doFinal(pack(accessInfo)));
        } catch (Exception e) {
            return null;
        }
    }

    public static AccessInfo decrypt(String encrypted) {
        try {
            byte[] data = ByteArrayTranslator.fromURLFormedBase64(encrypted);
            Cipher cipher =
                    CipherUtils.Symmetric.createDecrypter(ACCESS_INFO_KEY,
                            ACCESS_INFO_PARAM);
            return unpack(cipher.doFinal(data));
        } catch (Exception e) {
            return null;
        }
    }
}
