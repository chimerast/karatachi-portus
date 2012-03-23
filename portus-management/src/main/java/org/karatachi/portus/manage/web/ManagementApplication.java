package org.karatachi.portus.manage.web;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.util.lang.PackageName;
import org.karatachi.daemon.DaemonGroup;
import org.karatachi.daemon.DaemonManager;
import org.karatachi.daemon.DaemonManager.UniqueDaemonGroup;
import org.karatachi.portus.core.auth.Authorize;
import org.karatachi.portus.core.dto.AccountDto;
import org.karatachi.portus.core.type.AccountRole;
import org.karatachi.portus.manage.daemon.DirectReplicationDaemonGroup;
import org.karatachi.portus.manage.daemon.ForceReplicationDaemonGroup;
import org.karatachi.portus.manage.daemon.MonitorDaemonGroup.PortusMonitorDaemon;
import org.karatachi.portus.manage.daemon.MonitorDaemonGroup.ServerMonitorDaemon;
import org.karatachi.portus.manage.daemon.NodeCheckDaemonGroup;
import org.karatachi.portus.manage.daemon.NodeMaintenanceDaemonGroup;
import org.karatachi.portus.manage.daemon.ReplicationDaemonGroup;
import org.karatachi.portus.manage.daemon.S2DaemonManager;
import org.karatachi.portus.manage.web.node.NodeStatusPage;
import org.karatachi.portus.manage.web.top.IndexPage;
import org.karatachi.portus.webbase.web.PortusSession;
import org.karatachi.portus.webbase.web.auth.SignInPage;
import org.karatachi.portus.webbase.web.error.AccessDeniedPage;
import org.karatachi.portus.webbase.web.error.InternalErrorPage;
import org.karatachi.portus.webbase.web.error.PageExpiredErrorPage;
import org.karatachi.wicket.auth.AuthenticatedWebApplication;
import org.karatachi.wicket.auth.AuthenticatedWebSession;
import org.karatachi.wicket.util.ApplicationSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ManagementApplication extends
        AuthenticatedWebApplication<AccountDto, Authorize> {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    protected void init() {
        getApplicationSettings().setAccessDeniedPage(AccessDeniedPage.class);
        getApplicationSettings().setPageExpiredErrorPage(
                PageExpiredErrorPage.class);
        getApplicationSettings().setInternalErrorPage(InternalErrorPage.class);

        getDebugSettings().setAjaxDebugModeEnabled(false);

        mount("/auth", PackageName.forClass(SignInPage.class));
        mount("/error", PackageName.forClass(InternalErrorPage.class));

        mountBookmarkablePage("/nodestatus", NodeStatusPage.class);

        try {
            addDaemon(new NodeCheckDaemonGroup(), 4);
            addDaemon(new NodeMaintenanceDaemonGroup(), 0);

            addDaemon(new ReplicationDaemonGroup(), 10);
            addDaemon(new DirectReplicationDaemonGroup(), 10);
            addDaemon(new ForceReplicationDaemonGroup(), 0);

            S2DaemonManager.addDaemon(new PortusMonitorDaemon());
            S2DaemonManager.addDaemon(new ServerMonitorDaemon());
            if (!ApplicationSettings.isDevelopment()) {
                DaemonManager.getDaemonGroup(UniqueDaemonGroup.class).startup();
            }
        } catch (Exception e) {
            logger.error("初期化エラー", e);
        }
    }

    private void addDaemon(DaemonGroup daemonGroup, int count) {
        S2DaemonManager.addDaemonGroup(daemonGroup);
        if (!ApplicationSettings.isDevelopment() && count > 0) {
            daemonGroup.startup();
            daemonGroup.setCount(count);
        }
    }

    @Override
    protected void onDestroy() {
        DaemonManager.shutdownAll();
        logger.debug("shutdown application");
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
