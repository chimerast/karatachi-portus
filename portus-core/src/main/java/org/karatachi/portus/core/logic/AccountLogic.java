package org.karatachi.portus.core.logic;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.SystemUtils;
import org.karatachi.portus.common.crypto.EncryptionUtils;
import org.karatachi.portus.core.AssemblyInfo;
import org.karatachi.portus.core.PortusRuntimeException;
import org.karatachi.portus.core.dao.AccountDao;
import org.karatachi.portus.core.dao.AccountRootMapDao;
import org.karatachi.portus.core.dao.CustomerDao;
import org.karatachi.portus.core.dao.DomainDao;
import org.karatachi.portus.core.entity.Account;
import org.karatachi.portus.core.entity.AccountRootMap;
import org.karatachi.portus.core.entity.Customer;
import org.karatachi.portus.core.entity.Domain;
import org.karatachi.portus.core.entity.File;
import org.seasar.framework.container.annotation.tiger.Binding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AccountLogic {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Binding
    private FileAccessLogic fileAccessLogic;
    @Binding
    private ValidationLogic validationLogic;

    @Binding
    private CustomerDao customerDao;
    @Binding
    private DomainDao domainDao;
    @Binding
    private AccountDao accountDao;
    @Binding
    private AccountRootMapDao accountRootMapDao;

    public List<Customer> getCustomers() {
        return customerDao.selectAll();
    }

    public List<Customer> getCustomers(long parentId) {
        return customerDao.selectInCustomer(parentId);
    }

    public Customer addCustomerTx(Customer customer) {
        validationLogic.checkModifyCustomerAuthority(customer);
        validationLogic.checkQuota(customer);
        customer.name = validationLogic.trimFull(customer.name);

        Customer insert = new Customer();
        insert.parentId = customer.parentId;
        insert.name = customer.name;
        insert.valid = true;
        insert.quota = customer.quota;
        insert.role = customer.role.clone();
        customerDao.insert(insert);
        return insert;
    }

    public Customer updateCustomerTx(Customer customer) {
        Customer update = customerDao.selectForUpdate(customer.id);

        validationLogic.checkModifyCustomerAuthority(update);
        validationLogic.checkQuota(customer);
        customer.name = validationLogic.trimFull(customer.name);

        if (customer.parentId != 0
                && customerDao.selectInCustomer(customer.id).size() > 0) {
            throw new PortusRuntimeException("すでに子契約がある契約に親契約を設定することはできません。",
                    HttpURLConnection.HTTP_BAD_REQUEST);
        }

        update.parentId = customer.parentId;
        update.name = customer.name;
        update.quota = customer.quota;
        update.role = customer.role.clone();
        customerDao.update(update);
        return update;
    }

    public void removeCustomerTx(Customer customer) {
        for (Domain domain : getDomains(customer)) {
            removeDomainTx(domain);
        }
        for (Account account : getAccounts(customer)) {
            removeAccountTx(account);
        }
        customerDao.delete(customer);
    }

    public List<Domain> getDomains(Customer customer) {
        return domainDao.selectInCustomer(customer.id);
    }

    public Domain addDomainTx(Domain domain, List<Account> accounts) {
        validationLogic.checkModifyDomainAuthority(domain);

        boolean autonaming = false;
        if (domain.name == null) {
            domain.name = RandomStringUtils.randomAlphanumeric(8);
            autonaming = true;
        } else {
            validationLogic.checkDomainName(domain.name);
        }

        Domain insert = new Domain();
        insert.customerId = domain.customerId;
        insert.name = domain.name;
        insert.valid = true;
        insert.quota = -1L;
        domainDao.insert(insert);

        fileAccessLogic.createDomainDirTx(insert);

        updateDomainAccountsTx(insert, accounts);

        if (autonaming) {
            insert.name = AssemblyInfo.ROOT_SERVER + "/" + insert.id;
            insert = updateDomainTx(insert, accounts);
        }

        return insert;
    }

    public Domain updateDomainTx(Domain domain, List<Account> accounts) {
        Domain update = domainDao.selectForUpdate(domain.id);

        validationLogic.checkDomainName(domain.name);
        validationLogic.checkModifyDomainAuthority(update);

        update.name = domain.name;
        domainDao.update(update);

        fileAccessLogic.updateDomainDirTx(domain);

        updateDomainAccountsTx(update, accounts);

        return update;
    }

    public void removeDomainTx(Domain domain) {
        Domain remove = domainDao.selectForUpdate(domain.id);

        validationLogic.checkModifyDomainAuthority(remove);

        fileAccessLogic.removeDomainDirTx(remove);

        domainDao.delete(remove);
    }

    public void updateDomainAccountsTx(Domain domain, List<Account> accounts) {
        File domaindir = fileAccessLogic.getDomainDir(domain);

        List<Account> current = getActiveAccounts(domain);
        List<Account> add = new ArrayList<Account>();
        List<Account> remove = new ArrayList<Account>();

        for (Account account : accounts) {
            if (!current.contains(account)) {
                add.add(account);
            }
        }
        for (Account account : current) {
            if (!accounts.contains(account)) {
                remove.add(account);
            }
        }

        for (Account account : add) {
            AccountRootMap accountRootMap = new AccountRootMap();
            accountRootMap.accountId = account.id;
            accountRootMap.fileId = domaindir.id;
            accountRootMapDao.insert(accountRootMap);
        }
        for (Account account : remove) {
            accountRootMapDao.deleteByAccount(account.id, domaindir.id);
        }
    }

    public List<Account> getAccounts(Customer customer) {
        return accountDao.selectInCustomer(customer.id);
    }

    public Account addAccountTx(Account account) {
        validationLogic.checkModifyAccountAuthority(account);
        validationLogic.checkAccountName(account.name);

        Account insert = new Account();
        insert.customerId = account.customerId;
        insert.name = account.name;
        insert.valid = true;
        insert.quota = -1L;
        insert.role = account.role;
        insert.password = EncryptionUtils.passwordDigest(account.password);
        insert.homedir = AssemblyInfo.PATH_RAW_DATA;
        accountDao.insert(insert);
        insert.homedir =
                String.format("%s/%08X", AssemblyInfo.PATH_RAW_DATA, insert.id);
        accountDao.update(insert);

        new java.io.File(insert.homedir).mkdirs();
        if (SystemUtils.IS_OS_LINUX) {
            try {
                Runtime.getRuntime().exec(
                        "chown nobody.nogroup " + insert.homedir).waitFor();
            } catch (Exception e) {
                throw new IllegalStateException(
                        "fail to chown home directory.", e);
            }
        }
        return insert;
    }

    public Account updateAccountTx(Account account) {
        Account update = accountDao.selectForUpdate(account.id);

        validationLogic.checkModifyAccountAuthority(update);
        validationLogic.checkAccountName(account.name);

        update.name = account.name;
        if (!account.password.startsWith("{sha1}")) {
            update.password = EncryptionUtils.passwordDigest(account.password);
        }
        update.role = account.role;
        accountDao.update(update);
        return update;
    }

    public Account updateAccountPasswordTx(Account account, String password) {
        Account update = accountDao.selectForUpdate(account.id);
        validationLogic.checkModifyPasswordAuthority(update);
        update.password = EncryptionUtils.passwordDigest(password);
        accountDao.update(update);
        return update;
    }

    public void removeAccountTx(Account account) {
        Account remove = accountDao.selectForUpdate(account.id);

        validationLogic.checkModifyAccountAuthority(remove);

        accountDao.delete(remove);
        try {
            FileUtils.deleteDirectory(new java.io.File(account.homedir));
        } catch (IOException e) {
        }
    }

    public List<Account> getActiveAccounts(Domain domain) {
        File domaindir = fileAccessLogic.getDomainDir(domain);
        List<Account> ret = new ArrayList<Account>();
        for (AccountRootMap map : accountRootMapDao.selectInRoot(domaindir.id)) {
            ret.add(accountDao.select(map.accountId));
        }
        return ret;
    }

    public void addNewCustomerTx(Customer customer, Domain domain,
            Account account) {
        List<Account> accounts = new ArrayList<Account>();

        customer.id = addCustomerTx(customer).id;

        domain.customerId = customer.id;
        Domain createdDomain = addDomainTx(domain, accounts);
        domain.id = createdDomain.id;
        domain.name = createdDomain.name;

        boolean autonaming = false;
        if (account.name == null) {
            account.name = RandomStringUtils.randomAlphabetic(8);
            autonaming = true;
        }
        if (account.password == null) {
            account.password = RandomStringUtils.randomAlphabetic(8);
        }
        account.customerId = customer.id;
        account.id = addAccountTx(account).id;
        if (autonaming) {
            account.name = String.format("admin%04d", account.id);
            updateAccountTx(account);
        }

        accounts.add(account);
        updateDomainAccountsTx(domain, accounts);
    }
}
