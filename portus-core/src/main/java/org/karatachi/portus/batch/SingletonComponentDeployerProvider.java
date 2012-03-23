package org.karatachi.portus.batch;

import org.seasar.framework.container.ComponentDef;
import org.seasar.framework.container.ComponentDeployer;
import org.seasar.framework.container.deployer.ComponentDeployerFactory;
import org.seasar.framework.container.deployer.SingletonComponentDeployer;

public class SingletonComponentDeployerProvider extends
        ComponentDeployerFactory.DefaultProvider {
    @Override
    public ComponentDeployer createRequestComponentDeployer(
            final ComponentDef cd) {
        return new SingletonComponentDeployer(cd);
    }

    @Override
    public ComponentDeployer createSessionComponentDeployer(
            final ComponentDef cd) {
        return new SingletonComponentDeployer(cd);
    }

    @Override
    public ComponentDeployer createApplicationComponentDeployer(
            final ComponentDef cd) {
        return new SingletonComponentDeployer(cd);
    }
}
