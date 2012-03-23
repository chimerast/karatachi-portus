package org.karatachi.portus.webbase.web;

import org.apache.wicket.Request;
import org.karatachi.portus.core.AssemblyInfo;
import org.karatachi.portus.core.dto.AccountDto;
import org.karatachi.portus.core.logic.AuthorizationLogic;
import org.karatachi.wicket.auth.AuthenticatedWebSession;
import org.karatachi.wicket.util.ApplicationSettings;
import org.seasar.framework.container.SingletonS2Container;

public class PortusSession extends AuthenticatedWebSession<AccountDto> {
    private static final long serialVersionUID = 1L;

    public PortusSession(Request request) {
        super(request);
        setStyle(AssemblyInfo.SERVICE_NAME);

        if (ApplicationSettings.isDevelopment()) {
            signIn("root", "portus");
            //signIn("artificer", "artificer");
        }
    }

    @Override
    public AccountDto getRole() {
        return SingletonS2Container.getComponent(AccountDto.class);
    }

    @Override
    protected boolean authenticate(String username, String password) {
        AuthorizationLogic authorizationLogic =
                SingletonS2Container.getComponent(AuthorizationLogic.class);
        return authorizationLogic.authenticate(username, password);
    }
}
