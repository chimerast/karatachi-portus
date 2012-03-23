package org.karatachi.portus.core.dxo;

import java.util.List;

import org.karatachi.portus.core.dto.NodeFileDto;
import org.karatachi.portus.core.entity.File;
import org.karatachi.portus.core.entity.Storedinfo;
import org.seasar.extension.dxo.annotation.ConversionRule;

public interface NodeFileDxo {
    @ConversionRule("name : file.name, filename : file.filename, size : file.size, fullPath : file.fullPath")
    public List<NodeFileDto> convertFromStoredinfo(List<Storedinfo> storedinfo);

    @ConversionRule("'id' : -1, 'fileId' : id, 'chassisId' : -1, 'fromChassisId' : -1")
    public List<NodeFileDto> convertFromFile(List<File> file);
}
