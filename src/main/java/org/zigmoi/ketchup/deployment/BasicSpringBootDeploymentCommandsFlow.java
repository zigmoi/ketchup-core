package org.zigmoi.ketchup.deployment;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.zigmoi.ketchup.common.ConfigUtility;
import org.zigmoi.ketchup.common.FileUtility;
import org.zigmoi.ketchup.common.GitUtils;
import org.zigmoi.ketchup.deployment.model.MCArgBuildSpringBootDockerImageV1;
import org.zigmoi.ketchup.deployment.model.MCArgMvnInstallV1;
import org.zigmoi.ketchup.deployment.model.MCArgPullFromRemoteV1;
import org.zigmoi.ketchup.deployment.model.MCommandStatus;
import org.zigmoi.ketchup.exception.KUnexpectedException;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

public class BasicSpringBootDeploymentCommandsFlow implements IBasicSpringBootDeploymentCommands {

    private static AtomicLong seq = new AtomicLong(new Random().nextInt());

    @Override
    public MCommandStatus pullFromRemote(Object arg) {
        if (arg instanceof MCArgPullFromRemoteV1) {
            return execPullFromRemoteV1((MCArgPullFromRemoteV1) arg);
        } else {
            throw new UnsupportedOperationException("Unknown arg instance : " + arg);
        }
    }

    @Override
    public MCommandStatus mvnInstall(Object arg) {
        if (arg instanceof MCArgMvnInstallV1) {
            return execMvnInstallV1((MCArgMvnInstallV1) arg);
        } else {
            throw new UnsupportedOperationException("Unknown arg instance : " + arg);
        }
    }

    @Override
    public MCommandStatus buildSprintBootDockerImage(Object arg) {
        if (arg instanceof MCArgBuildSpringBootDockerImageV1) {
            return execBuildSpringBootDockerImageV1((MCArgBuildSpringBootDockerImageV1) arg);
        } else {
            throw new UnsupportedOperationException("Unknown arg instance : " + arg);
        }
    }

    @Override
    public MCommandStatus deployInKubernetes(Object arg) {
        return null;
    }

    // versioned arg schema implementations execMvnInstallV1 starts
    protected MCommandStatus execMvnInstallV1(MCArgMvnInstallV1 arg) {
        MCommandStatus commandStatus = new MCommandStatus();
        try {
            ProcessBuilder processBuilder = new ProcessBuilder();
            processBuilder.command(getCommandFromTemplateForMvnInstallV1(arg));
            Process process = processBuilder.start();
            BufferedReader inputReader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));
            BufferedReader errorReader = new BufferedReader(
                    new InputStreamReader(process.getErrorStream()));
            String line;
            while ((line = inputReader.readLine()) != null) {
                commandStatus.addLog(line);
            }
            while ((line = errorReader.readLine()) != null) {
                commandStatus.addLog(line);
            }
            int exitVal = process.waitFor();
            commandStatus.addLog("EXIT CODE = " + exitVal);
            if (exitVal == 0) {
                commandStatus.setSuccessful(true);
            } else {
                commandStatus.setSuccessful(false);
            }
        } catch (Exception e) {
            commandStatus.setSuccessful(false);
            commandStatus.addLog(ExceptionUtils.getStackTrace(e));
        }
        return commandStatus;
    }

    private String[] getCommandFromTemplateForMvnInstallV1(MCArgMvnInstallV1 arg) throws IOException { // todo script file cleanup
        String commandString = FileUtility.readDataFromFile(
                ConfigUtility.instance().getProperty(DeploymentFlowConstants.TP_MVN_CLEAN_INSTALL)
        ).replace("${maven-private-repo-settings-path}", arg.getPrivateRepoSettingsPath())
                .replace("${maven-command-path}", arg.getMvnCommandPath());
        String scriptPath = ConfigUtility.instance().getTmpDir() + File.separator + "ketchup-dt-" + seq.incrementAndGet();
        FileUtility.createAndWrite(new File(scriptPath), commandString);
        return new String[]{"bash", scriptPath};
    }
    // versioned arg schema implementations execMvnInstallV1 ends

    // versioned arg schema implementations execPullFromRemoteV1 starts
    protected MCommandStatus execPullFromRemoteV1(MCArgPullFromRemoteV1 arg) {
        MCommandStatus commandStatus = new MCommandStatus();
        try {
            if (arg.getRepoPath().exists()) {
                GitUtils.instance(arg.getUsername(), arg.getPassword())
                        .pull(arg.getRepoPath());
            } else {
                if (arg.getRepoPath().mkdirs()) {
                    GitUtils.instance(arg.getUsername(), arg.getPassword())
                            .clone(arg.getUrl(), arg.getRepoPath().getAbsolutePath());
                } else {
                    throw new KUnexpectedException("Failed to create directory : " + arg.getRepoPath());
                }
            }
            commandStatus.setSuccessful(true);
        } catch (Exception e) {
            commandStatus.setSuccessful(false);
            commandStatus.addLog(ExceptionUtils.getStackTrace(e));
        }
        return commandStatus;
    }
    // versioned arg schema implementations execPullFromRemoteV1 ends

    // versioned arg schema implementations execBuildSpringBootDockerImageV1 starts
    protected MCommandStatus execBuildSpringBootDockerImageV1(MCArgBuildSpringBootDockerImageV1 arg) {
        return null;
    }
    // versioned arg schema implementations execBuildSpringBootDockerImageV1 ends
}
