package org.karatachi.portus.manage.daemon;

import org.karatachi.daemon.Daemon;
import org.karatachi.daemon.DaemonGroup;
import org.karatachi.daemon.DaemonManager;
import org.seasar.framework.container.S2Container;
import org.seasar.framework.container.factory.SingletonS2ContainerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class S2DaemonManager {
    public static DaemonGroup addDaemonGroup(DaemonGroup daemonGroup) {
        Logger logger = LoggerFactory.getLogger(S2DaemonManager.class);
        logger.info("{} registered.", daemonGroup.getGroupName());
        injectDependency(daemonGroup);
        DaemonManager.addDaemonGroup(daemonGroup);
        return daemonGroup;
    }

    public static Daemon addDaemon(Daemon daemon) {
        Logger logger = LoggerFactory.getLogger(S2DaemonManager.class);
        logger.info("{} registered.", daemon.getName());
        injectDependency(daemon);
        DaemonManager.addUniqueDaemon(daemon);
        return daemon;
    }

    public static <T> T injectDependency(T obj) {
        S2Container container = SingletonS2ContainerFactory.getContainer();
        if (container.hasComponentDef(obj.getClass())) {
            container.injectDependency(obj);
        }
        return obj;
    }
}
