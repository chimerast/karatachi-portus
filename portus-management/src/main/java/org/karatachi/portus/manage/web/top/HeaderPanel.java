package org.karatachi.portus.manage.web.top;

import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.karatachi.portus.webbase.web.auth.SignOutPage;

public class HeaderPanel extends Panel {
    private static final long serialVersionUID = 1L;

    public HeaderPanel(String id) {
        super(id);
        add(new BookmarkablePageLink<Void>("signout", SignOutPage.class));
    }
}
