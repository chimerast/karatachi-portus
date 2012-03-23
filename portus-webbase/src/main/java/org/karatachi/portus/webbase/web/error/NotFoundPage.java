package org.karatachi.portus.webbase.web.error;


public class NotFoundPage extends ErrorBasePage {
    private static final long serialVersionUID = 1L;

    @Override
    protected String getPageTitle() {
        return "無効なURL";
    }
}
