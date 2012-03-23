package org.karatachi.portus.admin.web.account;

import org.karatachi.portus.admin.web.PortusBasePage;
import org.karatachi.portus.core.auth.Authorize;
import org.karatachi.portus.core.dto.AccountDto;
import org.karatachi.portus.core.type.AccountRole;
import org.seasar.framework.container.annotation.tiger.Binding;

@Authorize(AccountRole.Bit.MODIFY_ACCOUNT)
public class AccountPage extends PortusBasePage {
    @Binding
    private AccountDto accountDto;

    public AccountPage() {
        add(new AccountPanel("account", accountDto.getCustomer()));
    }

    @Override
    protected String getPageTitle() {
        return "アカウント管理";
    }
}
