package org.karatachi.portus.webbase.web.error;


public class AccessDeniedPage extends ErrorBasePage {
    private static final long serialVersionUID = 1L;

    @Override
    protected String getPageTitle() {
        return "アクセス拒否";
    }
}
