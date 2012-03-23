package org.karatachi.portus.manage.update;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;

import org.apache.commons.io.FileUtils;
import org.karatachi.translator.ByteArrayTranslator;

public class SystemFile {
    private File file;
    private int revision;
    private final String md5;

    public SystemFile(File file) throws IOException {
        String filename = file.getName().toLowerCase();
        if (!(filename.endsWith(".zip") || filename.endsWith(".jar"))) {
            throw new FileNotFoundException("zip|jarファイルではありません。Filename: "
                    + file.getName());
        } else {
            this.file = file;
            try {
                this.revision =
                        Integer.parseInt(filename.substring(0,
                                filename.length() - 4));
            } catch (NumberFormatException e) {
                throw new FileNotFoundException("ファイル名にリビジョンが含まれません。Filename: "
                        + file.getName());
            }
        }

        File md5file = new File(file.getParentFile(), file.getName() + ".MD5");
        if (!md5file.exists()) {
            throw new FileNotFoundException("ファイルが見つかりません。Filename: "
                    + md5file.getName());
        }
        byte[] md5 =
                ByteArrayTranslator.fromHex(FileUtils.readFileToString(md5file,
                        "UTF-8").trim());
        this.md5 = ByteArrayTranslator.toBase64(md5);
    }

    public FileInputStream getFileInputStream() throws IOException {
        return new FileInputStream(file);
    }

    public int getRevision() {
        return revision;
    }

    public String getMD5() {
        return md5;
    }

    public static SystemFile newestSystemFile(File dir) throws IOException {
        File[] files = dir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".zip")
                        || name.toLowerCase().endsWith(".jar");
            }
        });

        if (files.length == 0) {
            return null;
        }

        Arrays.sort(files, new Comparator<File>() {
            public int compare(File o1, File o2) {
                int r1 =
                        Integer.parseInt(o1.getName().substring(0,
                                o1.getName().length() - 4));
                int r2 =
                        Integer.parseInt(o2.getName().substring(0,
                                o2.getName().length() - 4));
                return r1 - r2;
            }
        });

        return new SystemFile(files[files.length - 1]);
    }
}
