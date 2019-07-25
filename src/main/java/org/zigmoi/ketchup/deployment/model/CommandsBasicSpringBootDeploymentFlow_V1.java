package org.zigmoi.ketchup.deployment.model;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.zigmoi.ketchup.common.ConfigUtility;
import org.zigmoi.ketchup.common.FileUtility;
import org.zigmoi.ketchup.common.GitUtils;
import org.zigmoi.ketchup.deployment.DeploymentFlowConstants;
import org.zigmoi.ketchup.deployment.ICommandsBasicSpringBootDeployment_V1;
import org.zigmoi.ketchup.exception.KUnexpectedException;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

public class CommandsBasicSpringBootDeploymentFlow_V1 implements ICommandsBasicSpringBootDeployment_V1 {

    private static AtomicLong seq = new AtomicLong(new Random().nextInt());

    @Override
    public MCommandStatus execPullFromRemote(Object arg) {
        if (arg instanceof MCArgPullFromRemoteV1) {
            return execPullFromRemoteV1((MCArgPullFromRemoteV1) arg);
        } else {
            throw new UnsupportedOperationException("Unknown arg instance : " + arg);
        }
    }

    @Override
    public MCommandStatus execMvnInstall(Object arg) {
        if (arg instanceof MCArgMvnInstallV1) {
            return execMvnInstallV1((MCArgMvnInstallV1) arg);
        } else {
            throw new UnsupportedOperationException("Unknown arg instance : " + arg);
        }
    }

    public MCommandStatus execMvnInstallV1(MCArgMvnInstallV1 arg) {
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
                commandStatus.addLogs(line);
            }
            while ((line = errorReader.readLine()) != null) {
                commandStatus.addLogs(line);
            }
            int exitVal = process.waitFor();
            commandStatus.addLogs("EXIT CODE = " + exitVal);
            if (exitVal == 0) {
                commandStatus.setSuccessful(true);
            } else {
                commandStatus.setSuccessful(false);
            }
        } catch (Exception e) {
            commandStatus.setSuccessful(false);
            commandStatus.addLogs(ExceptionUtils.getStackTrace(e));
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

    private MCommandStatus execPullFromRemoteV1(MCArgPullFromRemoteV1 arg) {
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
            commandStatus.addLogs(ExceptionUtils.getStackTrace(e));
        }
        return commandStatus;
    }
}
