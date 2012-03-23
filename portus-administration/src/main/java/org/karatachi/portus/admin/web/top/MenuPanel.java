package org.karatachi.portus.admin.web.top;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.karatachi.portus.core.dto.AccountDto;
import org.karatachi.portus.core.type.AccountRole.Bit;
import org.karatachi.portus.core.type.CustomerRole;
import org.seasar.framework.container.annotation.tiger.Binding;

public class MenuPanel extends Panel {
    private static final long serialVersionUID = 1L;

    @Binding
    private AccountDto accountDto;

    public MenuPanel(String id) {
        super(id);
        add(new WebMarkupContainer("manage").setVisible(accountDto.hasAccountRole(Bit.MODIFY_ACCOUNT)
                || accountDto.hasAccountRole(Bit.MODIFY_DOMAIN)
                || accountDto.hasAccountRole(Bit.MODIFY_CUSTOMER)
                || accountDto.hasAccountRole(Bit.ROOT)));
        add(new WebMarkupContainer("account").setVisible(accountDto.hasAccountRole(Bit.MODIFY_ACCOUNT)));
        add(new WebMarkupContainer("domain").setVisible(accountDto.hasAccountRole(Bit.MODIFY_DOMAIN)));
        add(new WebMarkupContainer("customer").setVisible(accountDto.hasAccountRole(Bit.ROOT)
                || (accountDto.hasCustomerRole(CustomerRole.Bit.HASCHILD) && accountDto.hasAccountRole(Bit.MODIFY_CUSTOMER))));
        add(new WebMarkupContainer("system").setVisible(accountDto.hasAccountRole(Bit.DEVEL)));
    }
}
