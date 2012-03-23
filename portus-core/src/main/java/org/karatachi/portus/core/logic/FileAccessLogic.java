package org.karatachi.portus.core.logic;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.karatachi.portus.common.crypto.EncryptionUtils;
import org.karatachi.portus.core.AssemblyInfo;
import org.karatachi.portus.core.PortusRuntimeException;
import org.karatachi.portus.core.auth.Authorize;
import org.karatachi.portus.core.dao.FileDao;
import org.karatachi.portus.core.dao.FileReplicationDao;
import org.karatachi.portus.core.dao.StoredinfoDao;
import org.karatachi.portus.core.dto.AccountDto;
import org.karatachi.portus.core.entity.Domain;
import org.karatachi.portus.core.entity.File;
import org.karatachi.portus.core.entity.FileReplication;
import org.karatachi.portus.core.entity.LocalFile;
import org.karatachi.portus.core.type.AccountRole;
import org.karatachi.portus.core.type.AccountRole.Bit;
import org.seasar.framework.container.annotation.tiger.Binding;
import org.seasar.framework.util.tiger.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileAccessLogic {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    public interface DirectoryCreationEventHandler {
        public void handle(File dir);
    }

    @Binding
    private ValidationLogic validationLogic;

    @Binding
    private FileDao fileDao;
    @Binding
    private FileReplicationDao fileReplicationDao;
    @Binding
    private StoredinfoDao storedinfoDao;

    @Binding
    private AccountDto accountDto;

    public File getAccountRoot() {
        if (accountDto.getRoots().size() != 0) {
            return fileDao.select(accountDto.getRoots().get(0));
        } else {
            return null;
        }
    }

    @Authorize(AccountRole.Bit.VIEW_FILE)
    public File getParentDir(File dir) {
        if (dir != null && !accountDto.getRoots().contains(dir.id)
                && dir.parentId != 0) {
            return fileDao.select(dir.parentId);
        } else {
            return null;
        }
    }

    @Authorize(AccountRole.Bit.VIEW_FILE)
    public File getDomainDir(Domain domain) {
        return fileDao.selectDomainDirectory(domain.id);
    }

    @Authorize(AccountRole.Bit.VIEW_FILE)
    public List<File> getDomainRootDirs() {
        List<File> ret = new ArrayList<File>();
        if (accountDto.hasAccountRole(Bit.ROOT)) {
            for (File domain : fileDao.selectFilesInDirectory(0)) {
                if (domain.id != 0) {
                    ret.add(domain);
                }
            }
        } else {
            for (long id : accountDto.getRoots()) {
                ret.add(fileDao.select(id));
            }
        }
        return ret;
    }

    @Authorize(AccountRole.Bit.VIEW_FILE)
    public File getDomainRootDir(File dir) {
        while (true) {
            File parent = getParentDir(dir);
            if (parent.parentId == 0) {
                return dir;
            } else {
                dir = parent;
            }
        }
    }

    @Authorize(AccountRole.Bit.VIEW_FILE)
    public File getFile(File file) {
        return fileDao.select(file.id);
    }

    @Authorize(AccountRole.Bit.VIEW_FILE)
    public List<File> getFiles(File dir) {
        return fileDao.selectFilesInDirectory(dir.id);
    }

    @Authorize(AccountRole.Bit.VIEW_FILE)
    public File getFileInDirectory(File dir, String name) {
        return fileDao.selectFileInDirectory(dir.id, name);
    }

    @Authorize(AccountRole.Bit.VIEW_FILE)
    public FileReplication getReplication(File file) {
        return fileReplicationDao.select(file.id);
    }

    @Authorize(AccountRole.Bit.MODIFY_DOMAIN)
    public File createDomainDirTx(Domain domain) {
        validationLogic.checkDomainName(domain.name);
        File file = new File();
        file.parentId = 0;
        file.domainId = domain.id;
        file.directory = true;
        file.name = domain.name;
        file.published = true;
        fileDao.insert(file);
        return file;
    }

    @Authorize(AccountRole.Bit.MODIFY_DOMAIN)
    public void updateDomainDirTx(Domain domain) {
        validationLogic.checkDomainName(domain.name);
        File file = fileDao.selectDomainDirectory(domain.id);
        file.name = domain.name;
        fileDao.update(file);
    }

    @Authorize(AccountRole.Bit.MODIFY_DOMAIN)
    public void removeDomainDirTx(Domain domain) {
        File file = fileDao.selectDomainDirectory(domain.id);
        fileDao.delete(file);
    }

    @Authorize(AccountRole.Bit.MODIFY_FILE)
    public File createDirTx(File dir, String name) {
        return createDirTx(dir, name, null);
    }

    @Authorize(AccountRole.Bit.MODIFY_FILE)
    public File createDirTx(File dir, String name, File attributes) {
        if (dir == null) {
            return null;
        }
        validationLogic.checkFileName(name);
        File file = fileDao.selectFileInDirectory(dir.id, name);
        if (file == null) {
            file = new File();
            file.parentId = dir.id;
            file.domainId = dir.domainId;
            file.directory = true;
            file.name = name;
            copyAttributes(file, attributes);
            fileDao.insert(file);
            return file;
        } else if (file.directory) {
            return file;
        } else {
            throw new PortusRuntimeException("すでに同じ名前のファイルがあります。ファイル名=" + name,
                    HttpURLConnection.HTTP_PRECON_FAILED);
        }
    }

    @Authorize(AccountRole.Bit.MODIFY_FILE)
    public File registerFileTx(File dir, LocalFile localFile)
            throws IOException {
        return registerFileTx(dir, localFile, null);
    }

    @Authorize(AccountRole.Bit.MODIFY_FILE)
    public File registerFileTx(File dir, LocalFile localFile, File attributes)
            throws IOException {
        if (!localFile.file.exists()) {
            throw new PortusRuntimeException("ファイルが見つかりません。",
                    HttpURLConnection.HTTP_NOT_FOUND);
        }
        return registerFileRecursive(dir, localFile.file, null, attributes);
    }

    @Authorize(AccountRole.Bit.MODIFY_FILE)
    public File registerFileWithNameTx(File dir, LocalFile localFile,
            String name, File attributes) throws IOException {
        if (!localFile.file.exists()) {
            throw new PortusRuntimeException("ファイルが見つかりません。",
                    HttpURLConnection.HTTP_NOT_FOUND);
        }
        return registerFileRecursive(dir, localFile.file, name, attributes);
    }

    private File registerFileRecursive(File parent, java.io.File f,
            String name, File attributes) {
        Map<java.io.File, java.io.File> moved =
                new HashMap<java.io.File, java.io.File>();
        try {
            return registerFileRecursiveInternal(parent, f, name, attributes,
                    moved);
        } catch (RuntimeException e) {
            // 移動したファイルのロールバック処理
            for (Map.Entry<java.io.File, java.io.File> entry : moved.entrySet()) {
                entry.getValue().renameTo(entry.getKey());
            }
            throw e;
        }
    }

    private File registerFileRecursiveInternal(File parent, java.io.File f,
            String name, File attributes, Map<java.io.File, java.io.File> moved) {
        if (f.isDirectory()) {
            validationLogic.checkFileName(f.getName());
            File dir = createDirTx(parent, f.getName(), attributes);
            if (dir != null) {
                for (java.io.File s : f.listFiles()) {
                    registerFileRecursiveInternal(dir, s, name, attributes,
                            moved);
                }
                if (f.listFiles().length == 0) {
                    f.delete();
                }
            }
            return dir;
        } else {
            if (name == null) {
                name = f.getName();
            }

            validationLogic.checkFileName(name);
            File file = fileDao.selectFileInDirectory(parent.id, name);
            if (file != null) {
                removeFileTx(file);
            }

            file = new File();
            file.parentId = parent.id;
            file.domainId = parent.domainId;
            file.directory = false;
            file.name = name;
            file.size = f.length();
            file.replication = AssemblyInfo.REPLICATION_DEFAULT;
            copyAttributes(file, attributes);
            fileDao.insert(file);

            java.io.File local =
                    new java.io.File(AssemblyInfo.PATH_REG_DATA,
                            EncryptionUtils.toFilePath(file.id));
            local.getParentFile().mkdirs();
            if (!f.renameTo(local)) {
                throw new PortusRuntimeException(
                        "ファイルの移動に失敗しました。管理者にお問い合わせください。",
                        HttpURLConnection.HTTP_INTERNAL_ERROR);
            }
            moved.put(f, local);
            return file;
        }
    }

    private void copyAttributes(File file, File attributes) {
        if (attributes != null) {
            file.published = attributes.published;
            file.authorized = attributes.authorized;
            file.openDate = attributes.openDate;
            file.closeDate = attributes.closeDate;
        }
    }

    @Authorize(AccountRole.Bit.MODIFY_FILE)
    public boolean updateFileTx(File file) {
        validationLogic.checkFileName(file.name);
        File update = fileDao.selectForUpdate(file.id);
        if (update == null) {
            return false;
        }
        update.name = file.name;
        update.published = file.published;
        update.authorized = file.authorized;
        update.openDate = file.openDate;
        update.closeDate = file.closeDate;
        update.referer = file.referer;
        fileDao.update(update);
        return true;
    }

    @Authorize(AccountRole.Bit.MODIFY_FILE)
    public boolean moveFileTx(File dir, File file, String path) {
        File current;
        if (path.startsWith("/")) {
            current = getDomainRootDir(dir);
        } else {
            current = dir;
        }

        for (String name : path.split("/")) {
            if (name.equals("") || name.equals(".")) {
                continue;
            } else if (name.equals("..")) {
                current = getParentDir(current);
                if (current == null) {
                    return false;
                } else {
                    continue;
                }
            } else {
                validationLogic.checkFileName(name);
                current = createDirTx(current, name);
                if (current == null) {
                    return false;
                }
            }
        }

        if (isExists(current.id, file.name)) {
            return false;
        } else if (file.id == current.id) {
            return false;
        }

        File update = fileDao.selectForUpdate(file.id);
        update.parentId = current.id;
        fileDao.update(update);
        return true;
    }

    @Authorize(AccountRole.Bit.MODIFY_FILE)
    public boolean removeFileTx(File file) {
        for (long root : accountDto.getRoots()) {
            if (file.id == root) {
                throw new PortusRuntimeException("ルートフォルダは削除できません。",
                        HttpURLConnection.HTTP_FORBIDDEN);
            }
        }
        return fileDao.delete(file) != 0;
    }

    @Authorize(AccountRole.Bit.MODIFY_FILE)
    public void setAttributeRecursive(File file, String fieldName, Object value) {
        try {
            setAttributeRecursive(file, File.class.getField(fieldName), value);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(
                    "Illegal access to field: fieldName=" + fieldName, e);
        } catch (NoSuchFieldException e) {
            throw new IllegalStateException("Unknown field: fieldName="
                    + fieldName, e);
        }
    }

    private void setAttributeRecursive(File file, Field field, Object value)
            throws IllegalAccessException {
        field.set(file, value);
        fileDao.update(file);
        if (file.directory) {
            for (File f : fileDao.selectFilesInDirectory(file.id)) {
                setAttributeRecursive(f, field, value);
            }
        }
    }

    @Authorize(AccountRole.Bit.MODIFY_FILE)
    public void setStreaming(File file, boolean value) {
        if (!file.directory) {
            if (value && file.fileTypeId == File.TYPE_NORMAL) {
                file.fileTypeId = File.TYPE_FLASH_STREAMING;
                fileDao.update(file);
                storedinfoDao.deleteByFile(file.id);
            } else if (!value && file.fileTypeId == File.TYPE_FLASH_STREAMING) {
                file.fileTypeId = File.TYPE_NORMAL;
                fileDao.update(file);
                storedinfoDao.deleteByFile(file.id);
            }
        }
    }

    private boolean isExists(long parentId, String name) {
        return fileDao.selectFileInDirectory(parentId, name) != null;
    }

    public List<Long> getFilesForCalculateDigest() {
        return fileDao.selectForCalculateDigest();
    }

    public void updateFileDigest(long id, long size, String digest) {
        fileDao.updateDigest(id, size, digest);
    }

    private Pair<File, String> extractPath(String path) {
        if (path.startsWith("/")) {
            path = path.substring(1);
        }

        for (long rootId : accountDto.getRoots()) {
            File dir = fileDao.select(rootId);
            if (path.startsWith(dir.fullPath + "/")) {
                String filepath = path.substring(dir.fullPath.length() + 1);
                return new Pair<File, String>(dir, filepath);
            }
        }

        throw new PortusRuntimeException("対応するドメインが見つかりません",
                HttpURLConnection.HTTP_NOT_ACCEPTABLE);
    }

    @Authorize(AccountRole.Bit.VIEW_FILE)
    public File findPath(String path) {
        Pair<File, String> root = extractPath(path);
        File current = root.getFirst();
        for (String name : root.getSecond().split("/")) {
            if (name.equals("") || name.equals(".") || name.equals("..")) {
                continue;
            } else {
                current = getFileInDirectory(current, name);
                if (current == null) {
                    throw new PortusRuntimeException("ファイルが見つかりません。",
                            HttpURLConnection.HTTP_NOT_FOUND);
                }
            }
        }
        return current;
    }

    @Authorize(AccountRole.Bit.MODIFY_FILE)
    public File createDirByPathTx(String path, File attributes) {
        Pair<File, String> root = extractPath(path);
        File current = root.getFirst();
        for (String name : root.getSecond().split("/")) {
            if (name.equals("") || name.equals(".") || name.equals("..")) {
                continue;
            } else {
                current = createDirTx(current, name, attributes);
            }
        }
        return current;
    }

    @Authorize(AccountRole.Bit.MODIFY_FILE)
    public void removeFileByPathTx(String path) {
        removeFileTx(findPath(path));
    }

    @Authorize(AccountRole.Bit.VIEW_FILE)
    public List<File> search(String path, Date registeredBefore,
            Date registeredAfter) {
        int idx = path.lastIndexOf("/");
        String dirpath = path.substring(0, idx + 1);
        String filename = path.substring(idx + 1);

        File dir = findPath(dirpath);
        return fileDao.search(dir.id, filename, registeredBefore,
                registeredAfter);
    }
}
