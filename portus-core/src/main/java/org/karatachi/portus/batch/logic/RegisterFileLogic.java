package org.karatachi.portus.batch.logic;

import java.io.IOException;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.karatachi.portus.batch.ProcessLogic;
import org.karatachi.portus.core.AssemblyInfo;
import org.karatachi.portus.core.dao.AccountRootMapDao;
import org.karatachi.portus.core.dao.FileDao;
import org.karatachi.portus.core.entity.AccountRootMap;
import org.karatachi.portus.core.entity.File;
import org.karatachi.portus.core.entity.LocalFile;
import org.karatachi.portus.core.logic.FileAccessLogic;
import org.seasar.framework.container.annotation.tiger.Binding;

public class RegisterFileLogic extends ProcessLogic {
    @Binding
    private AccountRootMapDao accountRootMapDao;
    @Binding
    private FileDao fileDao;
    @Binding
    private FileAccessLogic fileAccessLogic;

    @Override
    public void run() {
        if (args.length != 1) {
            throw new IllegalArgumentException("引数の数が違います。");
        }
        registerFile(args[0]);
    }

    public void registerFile(String source) {
        if (!source.startsWith(AssemblyInfo.PATH_RAW_DATA)) {
            throw new IllegalArgumentException("与えられたディレクトリが違います。");
        }

        String[] sourcedirs = source.split("/");
        if (sourcedirs.length < 5) {
            throw new IllegalArgumentException("アカウントディレクトリ以下のファイルを指定して下さい。");
        } else if (sourcedirs[3].length() != 8) {
            throw new IllegalArgumentException("アカウントディレクトリではありません。"
                    + sourcedirs[3]);
        } else if (StringUtils.isEmpty(sourcedirs[sourcedirs.length - 1])) {
            throw new IllegalArgumentException("パスの最後は\"/\"にしないで下さい。");
        }

        long accountId;
        try {
            accountId = Long.parseLong(sourcedirs[3], 16);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("");
        }

        List<AccountRootMap> roots =
                accountRootMapDao.selectInAccount(accountId);
        if (roots.size() != 1) {
            throw new IllegalArgumentException("アップロード先フォルダを特定できません。");
        }

        java.io.File file = new java.io.File(source);
        if (!file.exists()) {
            throw new IllegalArgumentException("アップロード元ファイルが見つかりません。");
        }

        File root = fileDao.select(roots.get(0).fileId);

        int i = 4;
        while (i < sourcedirs.length - 1) {
            root = fileAccessLogic.createDirTx(root, sourcedirs[i]);
            ++i;
        }

        LocalFile localFile = new LocalFile();
        localFile.file = file;
        try {
            fileAccessLogic.registerFileTx(root, localFile);
        } catch (IOException e) {
            throw new IllegalArgumentException("ファイルの登録に失敗しました。", e);
        }
    }
}
