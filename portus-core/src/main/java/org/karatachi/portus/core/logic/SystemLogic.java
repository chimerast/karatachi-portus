package org.karatachi.portus.core.logic;

import org.karatachi.portus.core.auth.Authorize;
import org.karatachi.portus.core.dao.ConfigDao;
import org.karatachi.portus.core.type.AccountRole;
import org.seasar.framework.container.annotation.tiger.Binding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SystemLogic {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Binding
    private ConfigDao configDao;

    @Authorize(AccountRole.Bit.VALIDUSER)
    public String getNotifyMessage() {
        return configDao.select("notify");
    }
}
