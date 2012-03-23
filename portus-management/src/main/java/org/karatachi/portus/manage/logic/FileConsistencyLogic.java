package org.karatachi.portus.manage.logic;

import java.util.ArrayList;
import java.util.List;

import org.karatachi.net.shell.CommandResponse;
import org.karatachi.portus.core.dao.StoredinfoDao;
import org.karatachi.portus.core.dto.NodeFileDto;
import org.karatachi.portus.core.dxo.NodeFileDxo;
import org.karatachi.portus.core.entity.Storedinfo;
import org.seasar.framework.container.annotation.tiger.Binding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileConsistencyLogic {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Binding
    private StoredinfoDao storedinfoDao;
    @Binding
    private NodeFileDxo nodeFileDxo;

    public List<NodeFileDto> readNodeFile(CommandResponse response) {
        if (response == null
                || response.getStatusCode() != CommandResponse.S_OK) {
            return null;
        }

        List<NodeFileDto> ret = new ArrayList<NodeFileDto>();

        for (String line : response.getBody()) {
            String[] data = line.split(",");
            if (data.length != 2) {
                logger.error("不正なファイルリスト: " + line);
                return null;
            }

            try {
                NodeFileDto file = new NodeFileDto();
                file.setFilename(data[0]);
                file.setSize(Long.parseLong(data[1]));
                ret.add(file);
            } catch (NumberFormatException e) {
                logger.error("ファイルサイズ変換エラー", e);
                return null;
            }
        }

        return ret;
    }

    public List<Storedinfo> getNodeFile(long nodeId) {
        return storedinfoDao.selectInNode(nodeId);
    }

    public List<NodeFileDto> getNodeFileWithPrefix(long nodeId, String prefix) {
        return nodeFileDxo.convertFromStoredinfo(storedinfoDao.selectInNodeWithPrefix(
                nodeId, prefix));
    }

    public boolean deleteFromNode(CommandResponse response) {
        return response != null
                && response.getStatusCode() == CommandResponse.S_OK;
    }

    public boolean deleteFromDatabase(long nodeId, long fileId) {
        return storedinfoDao.deleteFileInNode(nodeId, fileId) > 0;
    }
}
