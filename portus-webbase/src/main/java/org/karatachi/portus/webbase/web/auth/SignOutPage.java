package org.karatachi.portus.webbase.web.auth;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.karatachi.portus.webbase.web.WebBasePage;

public class SignOutPage extends WebBasePage {
    private static final long serialVersionUID = 1L;

    public SignOutPage(PageParameters parameters) {
        add(new BookmarkablePageLink<Void>("top",
                getApplication().getHomePage()));

        getSession().invalidate();
    }

    @Override
    protected String getPageTitle() {
        return "サインアウト";
    }
}
