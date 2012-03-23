package org.karatachi.portus.core.dto;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import org.karatachi.portus.common.crypto.EncryptionUtils;
import org.karatachi.portus.core.entity.Account;
import org.karatachi.portus.core.entity.Customer;
import org.karatachi.portus.core.type.AccountRole;
import org.karatachi.portus.core.type.CustomerRole;
import org.seasar.framework.container.annotation.tiger.Component;
import org.seasar.framework.container.annotation.tiger.InstanceType;

@Component(instance = InstanceType.SESSION)
public class AccountDto implements Serializable {
    private static final long serialVersionUID = 1L;

    private Customer customer;
    private Account account;
    private List<Long> roots;
    private String customercode;

    public void set(Customer customer, Account account, List<Long> roots) {
        this.customer = customer;
        this.account = account;
        this.roots = Collections.unmodifiableList(roots);
        this.customercode = EncryptionUtils.customerIdToString(customer.id);
    }

    public Customer getCustomer() {
        return customer;
    }

    public Account getAccount() {
        return account;
    }

    public List<Long> getRoots() {
        return roots;
    }

    public String getCustomerCode() {
        return customercode;
    }

    public boolean hasAccountRole(AccountRole.Bit bit) {
        if (account != null) {
            return account.role.hasBit(bit);
        } else {
            return false;
        }
    }

    public boolean hasCustomerRole(CustomerRole.Bit bit) {
        if (customer != null) {
            return customer.role.hasBit(bit);
        } else {
            return false;
        }
    }
}
