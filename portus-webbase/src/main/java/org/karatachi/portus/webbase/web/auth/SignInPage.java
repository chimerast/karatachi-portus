package org.karatachi.portus.webbase.web.auth;

import org.apache.wicket.PageParameters;
import org.karatachi.portus.webbase.web.WebBasePage;
import org.karatachi.wicket.auth.SignInPanel;

public class SignInPage extends WebBasePage {
    private static final long serialVersionUID = 1L;

    public SignInPage(PageParameters parameters) {
        add(new SignInPanel("signInPanel"));
    }

    @Override
    protected String getPageTitle() {
        return "サインイン";
    }
}
