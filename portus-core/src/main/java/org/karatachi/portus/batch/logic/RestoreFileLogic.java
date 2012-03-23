package org.karatachi.portus.batch.logic;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.karatachi.portus.batch.ProcessLogic;
import org.karatachi.portus.core.AssemblyInfo;
import org.karatachi.portus.core.dao.FileDao;
import org.karatachi.portus.core.entity.Account;
import org.karatachi.portus.core.entity.Customer;
import org.karatachi.portus.core.entity.Domain;
import org.karatachi.portus.core.entity.LocalFile;
import org.karatachi.portus.core.logic.AccountLogic;
import org.karatachi.portus.core.logic.FileAccessLogic;
import org.karatachi.portus.core.type.AccountRole;
import org.karatachi.portus.core.type.CustomerRole;
import org.seasar.framework.container.annotation.tiger.Binding;

public class RestoreFileLogic extends ProcessLogic {
    @Binding
    private AccountLogic accountLogic;
    @Binding
    private FileAccessLogic fileAccessLogic;

    @Binding
    private FileDao fileDao;

    @Override
    public void run() {
        File rootDir = new File(args[0]);
        if (!rootDir.isDirectory()) {
            logger.error("基点ディレクトリがありません: " + rootDir.getAbsolutePath());
            return;
        }

        File domainDir = null;
        for (File f : rootDir.listFiles()) {
            if (f.isDirectory()) {
                domainDir = f;
                break;
            }
        }
        if (domainDir == null) {
            logger.error("ドメインディレクトリがありません: " + rootDir.getAbsolutePath());
            return;
        }

        File csv =
                new File(domainDir.getParentFile(), domainDir.getName()
                        + ".csv");
        if (!csv.isFile()) {
            logger.error("csvファイルがありません: " + csv.getAbsolutePath());
            return;
        }

        File customertxt = new File(domainDir.getParentFile(), "customer.txt");
        if (!customertxt.isFile()) {
            logger.error("customerファイルがありません: " + customertxt.getAbsolutePath());
            return;
        }

        String customerName;
        try {
            customerName =
                    (String) FileUtils.readLines(customertxt, "UTF-8").get(0);
        } catch (Exception e) {
            customerName = domainDir.getName();
        }

        String domainName = domainDir.getName().replace("_", "/");

        ArrayList<Account> accounts = new ArrayList<Account>();

        Customer customer = new Customer();
        customer.name = customerName;
        customer.role = CustomerRole.DEFAULT;
        customer = accountLogic.addCustomerTx(customer);
        logger.info("Registered customer: id={}, name={}", customer.id,
                customer.name);

        Domain domain = new Domain();
        domain.customerId = customer.id;
        domain.name = domainName;
        domain = accountLogic.addDomainTx(domain, accounts);
        if (domain.name.startsWith("digital-data-dam.net/")) {
            domain.name = AssemblyInfo.ROOT_SERVER + "/" + domain.id;
            accountLogic.updateDomainTx(domain, accounts);
        }
        logger.info("Registered domain: id={}, name={}", domain.id, domain.name);

        Account account = new Account();
        account.customerId = customer.id;
        account.name = RandomStringUtils.randomAlphanumeric(8);
        account.password = RandomStringUtils.randomAlphanumeric(8);
        account.role = AccountRole.DEFAULT;
        account = accountLogic.addAccountTx(account);
        account.name = String.format("admin%04d", account.id);
        account = accountLogic.updateAccountTx(account);
        logger.info("Registered account: name={}, password={}", account.name,
                account.password);

        accounts.add(account);
        accountLogic.updateDomainAccountsTx(domain, accounts);

        org.karatachi.portus.core.entity.File dir =
                fileAccessLogic.getDomainDir(domain);

        try {
            for (File file : domainDir.listFiles()) {
                LocalFile localFile = new LocalFile();
                localFile.file = file;
                fileAccessLogic.registerFileTx(dir, localFile);
            }
        } catch (IOException e) {
            logger.error("ファイルの移動に失敗しました。", e);
        }

        try {
            for (String line : (List<String>) FileUtils.readLines(csv)) {
                String[] cols = line.split("\t");
                org.karatachi.portus.core.entity.File file =
                        fileDao.selectByFullPath(cols[1]);
                if (file == null) {
                    int idx = cols[1].lastIndexOf("/");
                    org.karatachi.portus.core.entity.File parent =
                            fileDao.selectByFullPath(cols[1].substring(0, idx));
                    fileAccessLogic.createDirTx(parent,
                            cols[1].substring(idx + 1));
                    file = fileDao.selectByFullPath(cols[1]);
                }
                file.published = cols[3].equalsIgnoreCase("true");
                file.authorized = cols[4].equalsIgnoreCase("true");
                fileDao.update(file);
            }
        } catch (IOException e) {
            logger.error("csvファイルの読み取りにしっぱしました");
        }
    }
}
