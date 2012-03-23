package org.karatachi.portus.batch;

import java.util.Arrays;

import org.karatachi.classloader.PackageDir;
import org.karatachi.portus.core.logic.AuthorizationLogic;
import org.seasar.extension.dbcp.impl.XADataSourceImpl;
import org.seasar.framework.container.SingletonS2Container;
import org.seasar.framework.container.deployer.ComponentDeployerFactory;
import org.seasar.framework.container.factory.SingletonS2ContainerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Bootstrap {
    private static final Logger logger =
            LoggerFactory.getLogger(Bootstrap.class);

    public static String[] args;

    public static void main(String[] args) {
        String database = "jdbc:postgresql://" + args[0] + "/portus";
        String command = args[1];

        Bootstrap.args = Arrays.copyOfRange(args, 2, args.length);
        try {
            ComponentDeployerFactory.setProvider(new SingletonComponentDeployerProvider());
            SingletonS2ContainerFactory.init();

            XADataSourceImpl xaDataSource =
                    SingletonS2Container.<XADataSourceImpl> getComponent("xaDataSource");
            xaDataSource.setURL(database);

            // rootアカウントで認証
            AuthorizationLogic authorizationLogic =
                    SingletonS2Container.getComponent(AuthorizationLogic.class);
            authorizationLogic.authenticateWithoutPassword("root");

            PackageDir logics =
                    new PackageDir("org.karatachi.portus.batch.logic");
            for (Class<? extends ProcessLogic> logic : logics.getClasses(ProcessLogic.class)) {
                if ((command + "Logic").equalsIgnoreCase(logic.getSimpleName())
                        || command.equalsIgnoreCase(logic.getSimpleName())) {
                    SingletonS2Container.getComponent(logic).run();
                    return;
                }
            }
        } catch (IllegalArgumentException e) {
            logger.error(e.getMessage());
        } catch (Exception e) {
            logger.error("Uncaught error.", e);
        }
    }
}
