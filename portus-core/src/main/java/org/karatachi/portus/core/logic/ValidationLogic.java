package org.karatachi.portus.core.logic;

import java.net.HttpURLConnection;
import java.util.List;

import org.karatachi.portus.core.PortusRuntimeException;
import org.karatachi.portus.core.auth.AuthorizationException;
import org.karatachi.portus.core.dao.CustomerDao;
import org.karatachi.portus.core.dto.AccountDto;
import org.karatachi.portus.core.dto.ValidationSettingsDto;
import org.karatachi.portus.core.entity.Account;
import org.karatachi.portus.core.entity.Customer;
import org.karatachi.portus.core.entity.Domain;
import org.karatachi.portus.core.type.AccountRole;
import org.karatachi.portus.core.type.AccountRole.Bit;
import org.karatachi.portus.core.type.CustomerRole;
import org.karatachi.text.UnicodeCharacter;
import org.seasar.framework.container.SingletonS2Container;
import org.seasar.framework.container.annotation.tiger.Binding;

public class ValidationLogic {
    @Binding
    private ValidationSettingsDto validationSettingsDto;

    public String trimFull(String value) {
        if (value == null) {
            return value;
        }

        String whitespaces = "[" + UnicodeCharacter.WHITE_SPACE_PATTERN + "]+";
        value = value.replaceAll("^" + whitespaces, "");
        value = value.replaceAll(whitespaces + "$", "");
        return value;
    }

    public void checkModifyCustomerAuthority(Customer customer) {
        AccountDto accountDto =
                SingletonS2Container.getComponent(AccountDto.class);
        if (accountDto.hasAccountRole(Bit.ROOT)) {
            ;
        } else if (!accountDto.hasAccountRole(AccountRole.Bit.MODIFY_CUSTOMER)) {
            throw new AuthorizationException();
        } else if (!accountDto.hasCustomerRole(CustomerRole.Bit.HASCHILD)) {
            throw new AuthorizationException();
        } else if (accountDto.getCustomer().id != customer.parentId) {
            throw new AuthorizationException();
        }

        if (customer.id == customer.parentId) {
            throw new PortusRuntimeException("自分自身を親にすることはできません。",
                    HttpURLConnection.HTTP_BAD_REQUEST);
        }
    }

    public void checkQuota(Customer customer) {
        if (customer.quota < 0) {
            throw new PortusRuntimeException("契約容量を設定してください。",
                    HttpURLConnection.HTTP_BAD_REQUEST);
        }

        if (customer.parentId == 0) {
            return;
        }

        CustomerDao customerDao =
                SingletonS2Container.getComponent(CustomerDao.class);

        Customer parent = customerDao.select(customer.parentId);
        List<Customer> children =
                customerDao.selectInCustomer(customer.parentId);

        long quota = 0;
        for (Customer child : children) {
            if (child.id == customer.id || child.quota == -1L) {
                continue;
            }
            quota += child.quota;
        }

        if (quota + customer.quota > parent.quota) {
            throw new PortusRuntimeException(
                    "親契約の契約容量を上回る容量を子契約に割り当てることはできません。",
                    HttpURLConnection.HTTP_BAD_REQUEST);
        }
    }

    public void checkModifyDomainAuthority(Domain domain) {
        AccountDto accountDto =
                SingletonS2Container.getComponent(AccountDto.class);
        if (accountDto.hasAccountRole(Bit.ROOT)) {
            ;
        } else if (!accountDto.hasAccountRole(AccountRole.Bit.MODIFY_DOMAIN)) {
            throw new AuthorizationException();
        } else if (accountDto.getCustomer().id != domain.customerId) {
            if (accountDto.hasCustomerRole(CustomerRole.Bit.HASCHILD)) {
                CustomerDao customerDao =
                        SingletonS2Container.getComponent(CustomerDao.class);
                if (accountDto.getCustomer().id != customerDao.select(domain.customerId).parentId) {
                    throw new AuthorizationException();
                }
            } else {
                throw new AuthorizationException();
            }
        }
    }

    public void checkModifyAccountAuthority(Account account) {
        AccountDto accountDto =
                SingletonS2Container.getComponent(AccountDto.class);
        if (accountDto.hasAccountRole(Bit.ROOT)) {
            ;
        } else if (!accountDto.hasAccountRole(AccountRole.Bit.MODIFY_ACCOUNT)) {
            throw new AuthorizationException();
        } else if (accountDto.getCustomer().id != account.customerId) {
            if (accountDto.hasCustomerRole(CustomerRole.Bit.HASCHILD)) {
                CustomerDao customerDao =
                        SingletonS2Container.getComponent(CustomerDao.class);
                if (accountDto.getCustomer().id != customerDao.select(account.customerId).parentId) {
                    throw new AuthorizationException();
                }
            } else {
                throw new AuthorizationException();
            }
        }
    }

    public void checkModifyPasswordAuthority(Account update) {
        AccountDto accountDto =
                SingletonS2Container.getComponent(AccountDto.class);
        if (accountDto.hasAccountRole(Bit.ROOT)) {
            ;
        } else if (accountDto.getAccount().id != update.id) {
            throw new AuthorizationException();
        }
    }

    public void checkDomainName(String name) {
        if (name == null || !name.matches("[-_.a-zA-Z0-9/]+")) {
            throw new PortusRuntimeException(
                    "ドメイン名に使用できない文字が指定されています(使用可能文字: a-zA-Z0-9._-/)。ドメイン名="
                            + name, HttpURLConnection.HTTP_BAD_REQUEST);
        }
    }

    public void checkAccountName(String name) {
        if (name == null || !name.matches("[_.a-zA-Z0-9]+")) {
            throw new PortusRuntimeException(
                    "アカウント名に使用できない文字が指定されています(使用可能文字: a-zA-Z0-9._)。アカウント名="
                            + name, HttpURLConnection.HTTP_BAD_REQUEST);
        }
    }

    public void checkFileName(String name) {
        if (!validationSettingsDto.relaxFilenameValidation) {
            if (name == null || !name.matches("[-_.a-zA-Z0-9]+")) {
                throw new PortusRuntimeException(
                        "ファイル名に使用できない文字が指定されています(使用可能文字: a-zA-Z0-9._-)。ファイル名="
                                + name, HttpURLConnection.HTTP_BAD_REQUEST);
            }
        } else {
            if (name == null
                    || !name.matches("[^/\\\\\\*\\?\"<>\\|#\\{\\}%&~]+")) {
                throw new PortusRuntimeException(
                        "ファイル名に使用できない文字が指定されています(使用可能文字: /\\:*?\"<>|#{}%&~)。ファイル名="
                                + name, HttpURLConnection.HTTP_BAD_REQUEST);
            }
        }
    }
}
