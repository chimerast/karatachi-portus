package org.karatachi.portus.webbase.web.error;

public class PageExpiredErrorPage extends ErrorBasePage {
    private static final long serialVersionUID = 1L;

    @Override
    protected String getPageTitle() {
        return "ページ有効期限切れ";
    }
}
