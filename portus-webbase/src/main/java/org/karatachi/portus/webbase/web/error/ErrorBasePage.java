package org.karatachi.portus.webbase.web.error;

import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.karatachi.portus.webbase.web.WebBasePage;

public class ErrorBasePage extends WebBasePage {
    private static final long serialVersionUID = 1L;

    public ErrorBasePage() {
        add(new BookmarkablePageLink<Void>("top",
                getApplication().getHomePage()));
    }
}
