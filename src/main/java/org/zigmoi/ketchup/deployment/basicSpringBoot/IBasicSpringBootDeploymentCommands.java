package org.zigmoi.ketchup.deployment.basicSpringBoot;

import org.zigmoi.ketchup.deployment.basicSpringBoot.model.MCommandStatus;

public interface IBasicSpringBootDeploymentCommands {

    MCommandStatus pullFromRemote(Object arg);

    MCommandStatus mvnInstall(Object arg);

    MCommandStatus buildSprintBootDockerImage(Object arg);

    MCommandStatus deployInKubernetes(Object arg);
}
