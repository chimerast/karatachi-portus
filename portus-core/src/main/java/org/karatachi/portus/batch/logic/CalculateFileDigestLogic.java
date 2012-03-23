package org.karatachi.portus.batch.logic;

import java.io.File;
import java.io.FileInputStream;
import java.security.MessageDigest;

import org.apache.commons.io.IOUtils;
import org.karatachi.crypto.DigestUtils;
import org.karatachi.portus.batch.ProcessLogic;
import org.karatachi.portus.common.crypto.EncryptionUtils;
import org.karatachi.portus.core.AssemblyInfo;
import org.karatachi.portus.core.logic.FileAccessLogic;
import org.karatachi.translator.ByteArrayTranslator;
import org.seasar.framework.container.annotation.tiger.Binding;

public class CalculateFileDigestLogic extends ProcessLogic {
    @Binding
    private FileAccessLogic fileAccessLogic;

    @Override
    public void run() {
        try {
            for (long id : fileAccessLogic.getFilesForCalculateDigest()) {
                String digestBase64 = null;
                File file =
                        new File(AssemblyInfo.PATH_REG_DATA,
                                EncryptionUtils.toFilePath(id));

                if (file.exists()) {
                    logger.info("計算開始: id={}, filepath={}", id,
                            file.getAbsolutePath());

                    FileInputStream in = null;
                    try {
                        in = new FileInputStream(file);
                        MessageDigest digest = DigestUtils.createDigest();

                        byte[] buf = new byte[8192];
                        int len = 0;
                        while ((len = in.read(buf)) != -1) {
                            digest.update(buf, 0, len);
                            if (Thread.interrupted()) {
                                throw new InterruptedException();
                            }
                        }

                        digestBase64 =
                                ByteArrayTranslator.toBase64(digest.digest());

                        fileAccessLogic.updateFileDigest(id, file.length(),
                                digestBase64);
                        logger.info("計算成功: id={}, digest={}", id, digestBase64);
                    } finally {
                        IOUtils.closeQuietly(in);
                    }
                } else {
                    fileAccessLogic.updateFileDigest(id, 0, null);
                    logger.info("ファイルが見つかりません: id={}, filepath={}", id,
                            file.getAbsolutePath());
                }
            }
        } catch (Exception e) {
            logger.error("計算失敗", e);
        }
    }
}
