package org.karatachi.portus.core.logic;

import java.util.ArrayList;
import java.util.List;

import org.karatachi.portus.common.crypto.EncryptionUtils;
import org.karatachi.portus.core.auth.Authorize;
import org.karatachi.portus.core.dao.AccountDao;
import org.karatachi.portus.core.dao.AccountRootMapDao;
import org.karatachi.portus.core.dao.CustomerDao;
import org.karatachi.portus.core.dto.AccountDto;
import org.karatachi.portus.core.entity.Account;
import org.karatachi.portus.core.entity.AccountRootMap;
import org.karatachi.portus.core.entity.Customer;
import org.seasar.framework.container.annotation.tiger.Binding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuthorizationLogic {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Binding
    private CustomerDao customerDao;
    @Binding
    private AccountRootMapDao domainAccountMapDao;
    @Binding
    private AccountDao accountDao;

    @Binding
    private AccountDto accountDto;

    @Authorize
    public boolean authenticate(String name, String password) {
        logger.debug("authenticate: username={}, password={}", name, password);
        Account account = accountDao.selectByName(name);
        if (account == null) {
            logger.debug("auth fail: account not found");
            return false;
        } else if (!account.password.equals(EncryptionUtils.passwordDigest(password))) {
            logger.debug("auth fail: database={}, input={}", account.password,
                    EncryptionUtils.passwordDigest(password));
            return false;
        }

        Customer customer = customerDao.select(account.customerId);

        List<AccountRootMap> map =
                domainAccountMapDao.selectInAccount(account.id);

        List<Long> roots = new ArrayList<Long>();
        for (AccountRootMap root : map) {
            roots.add(root.fileId);
        }

        accountDto.set(customer, account, roots);
        return true;
    }

    @Authorize
    public boolean authenticateWithoutPassword(String name) {
        logger.debug("authenticate without password: username={}", name);
        Account account = accountDao.selectByName(name);
        if (account == null) {
            logger.debug("auth fail: account not found");
            return false;
        }

        Customer customer = customerDao.select(account.customerId);

        List<AccountRootMap> map =
                domainAccountMapDao.selectInAccount(account.id);

        List<Long> roots = new ArrayList<Long>();
        for (AccountRootMap root : map) {
            roots.add(root.fileId);
        }

        accountDto.set(customer, account, roots);
        return true;
    }
}
