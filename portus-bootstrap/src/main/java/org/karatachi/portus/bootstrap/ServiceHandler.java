package org.karatachi.portus.bootstrap;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Comparator;

import org.karatachi.classloader.InstanceDelegator;
import org.karatachi.classloader.SecludedDirectoryClassLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServiceHandler {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private final SecludedDirectoryClassLoader loader;
    private final InstanceDelegator service;
    private final int nodeRevision;

    public ServiceHandler() throws FileNotFoundException,
            ClassNotFoundException {
        this(getLatestNodeDir());
    }

    /**
     * サービスクラスのロード
     * 
     * @param dir
     *            クラスローダに登録するディレクトリ
     * @return
     */
    public ServiceHandler(File dir) throws FileNotFoundException,
            ClassNotFoundException {
        loader = new SecludedDirectoryClassLoader(dir);
        service =
                loader.getStaticInterface("org.karatachi.portus.node.Service");
        nodeRevision = Integer.parseInt(dir.getName());
    }

    /**
     * Service.start()
     * 
     * @param clazz
     *            Serviceクラス
     * @return
     */
    public boolean start() {
        try {
            return (Boolean) service.invoke("start");
        } catch (Exception e) {
            logger.error("開始メソッドの呼び出しに失敗", e);
            return false;
        }
    }

    /**
     * Service.stop()
     * 
     * @param clazz
     *            Serviceクラス
     * @return
     */
    public boolean stop() {
        try {
            return (Boolean) service.invoke("stop");
        } catch (Exception e) {
            logger.error("終了メソッドの呼び出しに失敗", e);
            return false;
        }
    }

    /**
     * Service.join()
     * 
     * @param clazz
     *            Serviceクラス
     * @param timeout
     *            タイムアウト
     * @return
     */
    public boolean join(int timeout) {
        try {
            return (Boolean) service.invoke("join", timeout);
        } catch (Exception e) {
            logger.error("待機メソッドの呼び出しに失敗", e);
            return false;
        }
    }

    /**
     * Service.available()
     * 
     * @param clazz
     *            Serviceクラス
     * @return
     */
    public boolean available() {
        try {
            return (Boolean) service.invoke("available");
        } catch (Exception e) {
            logger.error("生存確認メソッドの呼び出しに失敗", e);
            return false;
        }
    }

    /**
     * システムリビジョンの取得
     * 
     * @return リビジョン
     */
    public int getNodeRevision() {
        return nodeRevision;
    }

    /**
     * 最新リビジョンのノードプログラムディレクトリの取得
     * 
     * @return
     */
    public static File getLatestNodeDir() {
        File nodeRoot = new File(AssemblyInfo.PATH_NODE);

        File[] revisions = nodeRoot.listFiles(new FileFilter() {
            public boolean accept(File pathname) {
                if (!pathname.isDirectory()) {
                    return false;
                }
                try {
                    new Integer(pathname.getName());
                    return true;
                } catch (NumberFormatException e) {
                    return false;
                }
            }
        });

        Arrays.sort(revisions, new Comparator<File>() {
            public int compare(File o1, File o2) {
                return new Integer(o1.getName()).compareTo(new Integer(
                        o2.getName()));
            }
        });

        if (revisions.length != 0) {
            return revisions[revisions.length - 1];
        } else {
            return null;
        }
    }

    /**
     * 最新リビジョン番号を取得
     * 
     * @return
     */
    public static int getLatestNodeRevision() {
        try {
            return Integer.parseInt(getLatestNodeDir().getName());
        } catch (Exception e) {
            return -1;
        }
    }
}
