package org.karatachi.portus.core.dao;

import org.karatachi.portus.core.entity.NodeEvent;
import org.seasar.extension.unit.S2TestCase;
import org.seasar.framework.container.annotation.tiger.Binding;

public class NodeDaoTest extends S2TestCase {
    @Binding
    private NodeDao nodeDao;

    @Override
    protected void setUp() throws Exception {
        include("app.dicon");
    }

    public void testSelectActive() {
        System.out.println(nodeDao.selectActive());
    }

    public void test() {
        System.out.println(nodeDao.selectByEvent(NodeEvent.BOOTSTRAP_UPDATE));
    }
}
