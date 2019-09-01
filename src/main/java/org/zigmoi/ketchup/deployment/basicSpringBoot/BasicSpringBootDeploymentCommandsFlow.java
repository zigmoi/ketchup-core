package org.zigmoi.ketchup.deployment.basicSpringBoot;

import io.kubernetes.client.ApiException;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.zigmoi.ketchup.common.*;
import org.zigmoi.ketchup.deployment.basicSpringBoot.model.*;
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
        if (arg instanceof MCArgDeploySpringBootOnKubernetesV1) {
            return execDeploySpringBootOnKubernetesV1((MCArgDeploySpringBootOnKubernetesV1) arg);
        } else {
            throw new UnsupportedOperationException("Unknown arg instance : " + arg);
        }
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
                System.out.println(line);
                commandStatus.addLog(line);
            }
            while ((line = errorReader.readLine()) != null) {
                System.out.println(line);
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
        String commandString = "cd " + arg.getBuildPath() + ";\n"
                + FileUtility.readDataFromFile(
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
                GitUtility.instance(arg.getGitVendorArg().getUsername(), arg.getGitVendorArg().getPassword())
                        .pull(arg.getRepoPath());
            } else {
                if (arg.getRepoPath().mkdirs()) {
                    GitUtility.instance(arg.getGitVendorArg().getUsername(), arg.getGitVendorArg().getPassword())
                            .clone(arg.getGitVendorArg().getUrl(), arg.getRepoPath().getAbsolutePath());
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
        MCommandStatus mCommandStatus = new MCommandStatus();
        try {
            createDockerFile(arg);

            Triple<String, String, String> dockerAccessDetails = null;
            String tag = null;
            if (DeploymentFlowConstants.DV_AWS_ECR.equalsIgnoreCase(arg.getDockerRegistryVendor())) {
                dockerAccessDetails = AWSECRUtility.getDockerAccessDetails(
                        arg.getDockerVendorArg().getRegistryId(),
                        arg.getDockerVendorArg().getAwsAccessKeyId(),
                        arg.getDockerVendorArg().getAwsSecretKey()
                );
                tag = AWSECRUtility.getTag(arg.getDockerVendorArg().getRegistryBaseUrl(),
                        arg.getDockerVendorArg().getRepo(), arg.getDockerBuildImageTag());
            }
            assert dockerAccessDetails != null;
            DockerUtility.buildAndPushImage(dockerAccessDetails.getLeft(),
                    dockerAccessDetails.getMiddle(),
                    dockerAccessDetails.getRight(),
                    tag,
                    new File(arg.getBasePath()),
                    new File(arg.getDockerFilePath()));
            mCommandStatus.setSuccessful(true);
        } catch (Exception e) {
            mCommandStatus.setSuccessful(false);
        }
        return mCommandStatus;
    }

    private void createDockerFile(MCArgBuildSpringBootDockerImageV1 arg) throws IOException {
        File dockerFilePath = new File(arg.getDockerFilePath());
        String templateData = FileUtility.readDataFromFile(arg.getDockerFileTemplatePath());
        String dockerFileContent = TemplateUtility.parse(templateData, arg.getDockerFileTemplateArgs());
        FileUtility.createAndWrite(dockerFilePath, dockerFileContent);
    }
    // versioned arg schema implementations execBuildSpringBootDockerImageV1 ends

    // versioned arg schema implementations execDeploySpringBootOnKubernetesV1 starts
    protected MCommandStatus execDeploySpringBootOnKubernetesV1(MCArgDeploySpringBootOnKubernetesV1 arg) {
        MCommandStatus commandStatus = new MCommandStatus();
        try {
            String tag = AWSECRUtility.getTag(arg.getDockerVendorArg().getRegistryBaseUrl(),
                    arg.getDockerVendorArg().getRepo(), arg.getDockerBuildImageTag());
            KubernetesUtility.deployInAws(new File(arg.getKubeconfigFilePath()),
                    arg.getNamespace(),
                    arg.getAppId(),
                    tag,
                    arg.getPort(),
                    arg.getIpHostnameMap()
            );
            commandStatus.setSuccessful(true);
        } catch (IOException | ApiException e) {
            e.printStackTrace();
            commandStatus.setSuccessful(false);
        }
        return commandStatus;
    }
    // versioned arg schema implementations execDeploySpringBootOnKubernetesV1 ends
}
