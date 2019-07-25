package org.zigmoi.ketchup.deployment;

import org.zigmoi.ketchup.deployment.model.MCommandStatus;

public interface ICommandsBasicSpringBootDeployment_V1 {

    MCommandStatus execPullFromRemote(Object arg);

    MCommandStatus execMvnInstall(Object arg);
}
