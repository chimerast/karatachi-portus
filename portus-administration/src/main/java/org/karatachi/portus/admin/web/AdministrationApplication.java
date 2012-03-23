package org.karatachi.portus.admin.web;

import org.apache.wicket.Page;
import org.apache.wicket.Request;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.Response;
import org.apache.wicket.Session;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.protocol.http.HttpSessionStore;
import org.apache.wicket.protocol.http.WebRequest;
import org.apache.wicket.protocol.http.WebRequestCycle;
import org.apache.wicket.protocol.http.WebRequestCycleProcessor;
import org.apache.wicket.protocol.http.WebResponse;
import org.apache.wicket.protocol.http.request.CryptedUrlWebRequestCodingStrategy;
import org.apache.wicket.protocol.http.request.InvalidUrlException;
import org.apache.wicket.protocol.http.request.WebRequestCodingStrategy;
import org.apache.wicket.request.IRequestCodingStrategy;
import org.apache.wicket.request.IRequestCycleProcessor;
import org.apache.wicket.session.ISessionStore;
import org.apache.wicket.util.lang.PackageName;
import org.karatachi.portus.admin.web.file.FileListPage;
import org.karatachi.portus.admin.web.manage.MemoryViewPage;
import org.karatachi.portus.admin.web.top.IndexPage;
import org.karatachi.portus.core.auth.Authorize;
import org.karatachi.portus.core.dto.AccountDto;
import org.karatachi.portus.core.type.AccountRole;
import org.karatachi.portus.webbase.web.PortusSession;
import org.karatachi.portus.webbase.web.auth.SignInPage;
import org.karatachi.portus.webbase.web.error.AccessDeniedPage;
import org.karatachi.portus.webbase.web.error.InternalErrorPage;
import org.karatachi.portus.webbase.web.error.NotFoundPage;
import org.karatachi.portus.webbase.web.error.PageExpiredErrorPage;
import org.karatachi.wicket.auth.AuthenticatedWebApplication;
import org.karatachi.wicket.auth.AuthenticatedWebSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AdministrationApplication extends
        AuthenticatedWebApplication<AccountDto, Authorize> {
    protected Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    protected void init() {
        getApplicationSettings().setAccessDeniedPage(AccessDeniedPage.class);
        getApplicationSettings().setPageExpiredErrorPage(
                PageExpiredErrorPage.class);
        getApplicationSettings().setInternalErrorPage(InternalErrorPage.class);

        getDebugSettings().setAjaxDebugModeEnabled(false);

        mount("/auth", PackageName.forClass(SignInPage.class));
        mount("/error", PackageName.forClass(InternalErrorPage.class));
        mount("/file", PackageName.forClass(FileListPage.class));
        mount("/manage", PackageName.forClass(MemoryViewPage.class));
    }

    @Override
    protected IRequestCycleProcessor newRequestCycleProcessor() {
        return new WebRequestCycleProcessor() {
            @Override
            protected IRequestCodingStrategy newRequestCodingStrategy() {
                return new CryptedUrlWebRequestCodingStrategy(
                        new WebRequestCodingStrategy());
            }
        };
    }

    @Override
    public RequestCycle newRequestCycle(Request request, Response response) {
        return new WebRequestCycle(this, (WebRequest) request,
                (WebResponse) response) {
            @Override
            public Page onRuntimeException(Page page, RuntimeException e) {
                if (e instanceof InvalidUrlException) {
                    return new NotFoundPage();
                }
                return super.onRuntimeException(page, e);
            }

            @Override
            protected void logRuntimeException(RuntimeException e) {
                if (e instanceof InvalidUrlException) {
                    return;
                }

                PortusSession session = (PortusSession) Session.get();

                Throwable t = e;
                while (t.getCause() != null) {
                    t = t.getCause();
                }

                if (session.isSignedIn()) {
                    String username = session.getRole().getAccount().name;
                    logger.error("[application] username=" + username, t);
                } else {
                    logger.error("[application] ", t);
                }
            }
        };
    }

    @Override
    protected ISessionStore newSessionStore() {
        if (DEPLOYMENT.equalsIgnoreCase(getConfigurationType())) {
            return super.newSessionStore();
        } else {
            return new HttpSessionStore(this);
        }
    }

    @Override
    public Class<? extends WebPage> getHomePage() {
        return IndexPage.class;
    }

    @Override
    protected boolean authorize(AccountDto role, Authorize annotation) {
        for (AccountRole.Bit bit : annotation.value()) {
            if (!role.hasAccountRole(bit)) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected Class<Authorize> getAnnotationClass() {
        return Authorize.class;
    }

    @Override
    protected Class<? extends AuthenticatedWebSession<AccountDto>> getWebSessionClass() {
        return PortusSession.class;
    }

    @Override
    protected Class<? extends WebPage> getSignInPageClass() {
        return SignInPage.class;
    }
}
