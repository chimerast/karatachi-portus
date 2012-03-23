package org.karatachi.portus.manage.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.karatachi.net.shell.CommandResponse;
import org.karatachi.portus.core.dao.NodeDao;
import org.karatachi.portus.core.entity.Node;
import org.karatachi.portus.manage.dto.NodeConditionDto;
import org.seasar.framework.container.annotation.tiger.Binding;
import org.seasar.framework.container.annotation.tiger.Component;
import org.seasar.framework.container.annotation.tiger.InitMethod;
import org.seasar.framework.container.annotation.tiger.InstanceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(instance = InstanceType.SINGLETON)
public class NodeManagementService {
    protected Logger logger = LoggerFactory.getLogger(getClass());

    @Binding
    private NodeDao nodeDao;

    private final Map<Long, NodeConditionDto> nodes;

    public NodeManagementService() {
        nodes =
                Collections.synchronizedMap(new HashMap<Long, NodeConditionDto>());
    }

    @InitMethod
    public void init() {
        synchronized (nodes) {
            for (Node node : nodeDao.selectActive()) {
                NodeConditionDto dto = new NodeConditionDto();
                dto.setNode(node);
                nodes.put(node.id, dto);
            }
        }
    }

    public void updateChassis(Node node, CommandResponse response) {
        synchronized (nodes) {
            NodeConditionDto dto = nodes.get(node.id);
            if (dto != null) {
                dto.setNode(node);
                dto.setPerformance(response);
            } else {
                dto = new NodeConditionDto();
                dto.setNode(node);
                dto.setPerformance(response);
                nodes.put(node.id, dto);
            }
        }
    }

    public void updateNodeStatus(long id, int status) {
        NodeConditionDto dto = nodes.get(id);
        if (dto != null) {
            int old = dto.getStatus();
            if (old == 0 || old == 1 || old == 2) {
                dto.setStatus(status);
            }
        }
    }

    public void updateNodeStatusForce(long id, int status) {
        NodeConditionDto dto = nodes.get(id);
        if (dto != null) {
            dto.setStatus(status);
        }
    }

    public NodeConditionDto getChassis(long id) {
        synchronized (nodes) {
            for (NodeConditionDto dto : nodes.values()) {
                if (dto.getNode().id == id) {
                    return dto;
                }
            }
        }
        return null;
    }

    public List<NodeConditionDto> getNodeList(
            Comparator<NodeConditionDto> sorting) {
        List<NodeConditionDto> ret = new ArrayList<NodeConditionDto>();
        synchronized (nodes) {
            for (NodeConditionDto dto : nodes.values()) {
                ret.add(dto);
            }
        }
        Collections.sort(ret, sorting);
        return ret;
    }

    public List<NodeConditionDto> getDownNodeList(
            Comparator<NodeConditionDto> sorting) {
        List<NodeConditionDto> ret = new ArrayList<NodeConditionDto>();
        synchronized (nodes) {
            for (NodeConditionDto dto : nodes.values()) {
                if (dto.getStatus() != Node.STATUS_OK) {
                    ret.add(dto);
                }
            }
        }
        Collections.sort(ret, sorting);
        return ret;
    }

    public long getTotalValue(String name) {
        long ret = 0;
        synchronized (nodes) {
            for (NodeConditionDto dto : nodes.values()) {
                if (dto.getStatus() == Node.STATUS_OK) {
                    Long value = dto.getPerformance().get(name);
                    if (value != null && value > 0) {
                        ret += value;
                    }
                }
            }
        }
        return ret;
    }

    public int getCount() {
        return nodes.size();
    }

    public int getAvailableCount() {
        int ret = 0;
        synchronized (nodes) {
            for (NodeConditionDto dto : nodes.values()) {
                if (dto.getStatus() == Node.STATUS_OK) {
                    ++ret;
                }
            }
        }
        return ret;
    }
}
