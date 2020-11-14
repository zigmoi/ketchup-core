package org.zigmoi.ketchup.helm.services;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang.text.StrSubstitutor;
import org.json.JSONArray;
import org.springframework.stereotype.Service;
import org.zeroturnaround.exec.InvalidExitValueException;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zigmoi.ketchup.helm.dtos.ChartInstallResponseDto;
import org.zigmoi.ketchup.helm.exceptions.CommandFailureException;
import org.zigmoi.ketchup.helm.dtos.ListAllChartsResponseDto;
import org.zigmoi.ketchup.helm.dtos.ReleaseStatusResponseDto;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeoutException;

@Log4j2
@Service
public class HelmServiceImpl implements HelmService {


    public static void main(String[] args) throws CommandFailureException {

        String chartName = "bitnami/tomcat";
        String releaseName = "tomcat-101";
        ChartInstallResponseDto installResponse = new HelmServiceImpl().installChart(chartName, releaseName);
        log.info("response: " + installResponse);

//        ChartInstallResponseDto installResponse = new HelmServiceImpl().installChart(chartName, releaseName, "/Users/neo/Downloads/helm/config.yaml");
//        log.info("response: " + installResponse);

//        ReleaseStatusResponseDto response = new HelmServiceImpl().getReleaseStatus(releaseName);
//        log.info("response: " + response.toString());

        List<ListAllChartsResponseDto> response1 = new HelmServiceImpl().listAllCharts();
        response1.stream().forEach(a -> log.info(a.toString()));

        //  new HelmServiceImpl().deleteRelease(releaseName);

        //Auto generate release name.
//        ChartInstallResponseDto installResponse1 = new HelmServiceImpl().installChart("bitnami/tomcat");
//        log.info("response: " + installResponse1);
//        String releaseName1 = installResponse1.getName();
//
//        ReleaseStatusResponseDto response11 = new HelmServiceImpl().getReleaseStatus(releaseName1);
//        log.info("response: " + response11.toString());
//
//        new HelmServiceImpl().deleteRelease(releaseName1);
    }


    @Override
    public String createChart() {
        return null;
    }

    @Override
    public ChartInstallResponseDto installChart(String chartName, String releaseName, String valuesYamlLocation) throws CommandFailureException {
        //Release name is name given to this instance of chart being deployed.
        //Version no is also present to identify it.
        Map<String, String> args = new HashMap<>();
        args.put("chartName", chartName);
        args.put("releaseName", releaseName);
        args.put("valuesYamlFileUrl", valuesYamlLocation); //can be location on filesystem or url.
        StrSubstitutor sub = new StrSubstitutor(args, "${", "}");
        String command = sub.replace("helm install ${releaseName} ${chartName} -f ${valuesYamlFileUrl}  -o json");
        String output = "";
        try {
            output = new ProcessExecutor().commandSplit(command)
                    .readOutput(true)
                    .exitValues(0) //valid exit codes, if other code is encountered exception will be raised.
                    .execute().outputString();
        } catch (InvalidExitValueException e) {
            log.debug("Command error code: " + e.getExitValue());
            log.debug("Command error message: " + e.getResult().outputString());
            log.error(e);
            throw new CommandFailureException("Command execution failed with non zero exit code, ", e);
        } catch (InterruptedException | IOException | TimeoutException e) {
            log.error(e);
            throw new CommandFailureException("Error encountered in executing command, ", e);
        }
        log.debug("Command output: " + output);
        ChartInstallResponseDto response = new ChartInstallResponseDto(output);
        return response;
    }

    @Override
    public ChartInstallResponseDto installChart(String chartName, String releaseName) throws CommandFailureException {
        //Release name is name given to this instance of chart being deployed.
        //Version no is also present to identify it.
        Map<String, String> args = new HashMap<>();
        args.put("chartName", chartName);
        args.put("releaseName", releaseName);
        StrSubstitutor sub = new StrSubstitutor(args, "${", "}");
        String command = sub.replace("helm install ${releaseName} ${chartName}  -o json");
        String output = "";
        try {
            output = new ProcessExecutor().commandSplit(command)
                    .readOutput(true)
                    .exitValues(0) //valid exit codes, if other code is encountered exception will be raised.
                    .execute().outputString();
        } catch (InvalidExitValueException e) {
            log.debug("Command error code: " + e.getExitValue());
            log.debug("Command error message: " + e.getResult().outputString());
            log.error(e);
            throw new CommandFailureException("Command execution failed with non zero exit code, ", e);
        } catch (InterruptedException | IOException | TimeoutException e) {
            log.error(e);
            throw new CommandFailureException("Error encountered in executing command, ", e);
        }
        log.debug("Command output: " + output);
        ChartInstallResponseDto response = new ChartInstallResponseDto(output);
        return response;
    }

    @Override
    public ChartInstallResponseDto installChart(String chartName) throws CommandFailureException {
        //Release name is name given to this instance of chart being deployed.
        //Version no is also present to identify it.
        Map<String, String> args = new HashMap<>();
        args.put("chartName", chartName);
        StrSubstitutor sub = new StrSubstitutor(args, "${", "}");
        String command = sub.replace("helm install ${chartName} --generate-name  -o json");
        String output = "";
        try {
            output = new ProcessExecutor().commandSplit(command)
                    .readOutput(true)
                    .exitValues(0) //valid exit codes, if other code is encountered exception will be raised.
                    .execute().outputString();
        } catch (InvalidExitValueException e) {
            log.debug("Command error code: " + e.getExitValue());
            log.debug("Command error message: " + e.getResult().outputString());
            log.error(e);
            throw new CommandFailureException("Command execution failed with non zero exit code, ", e);
        } catch (InterruptedException | IOException | TimeoutException e) {
            log.error(e);
            throw new CommandFailureException("Error encountered in executing command, ", e);
        }
        log.debug("Command output: " + output);
        ChartInstallResponseDto response = new ChartInstallResponseDto(output);
        return response;
    }

    @Override
    public ReleaseStatusResponseDto getReleaseStatus(String releaseName, String namespace, String kubeConfig) throws CommandFailureException {
        String kubeConfigPath = "";
        try {
            kubeConfigPath = createTempKubeconfig(kubeConfig);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Map<String, String> args = new HashMap<>();
        args.put("releaseName", releaseName);
        args.put("namespace", namespace);
        args.put("kubeConfigPath", kubeConfigPath);
        StrSubstitutor sub = new StrSubstitutor(args, "${", "}");
        String command = sub.replace("helm status ${releaseName} --namespace=${namespace} --kubeconfig=${kubeConfigPath} -o json");
        String output = "";
        try {
            output = new ProcessExecutor().commandSplit(command)
                    .readOutput(true)
                    .exitValues(0) //valid exit codes, if other code is encountered exception will be raised.
                    .execute().outputString();
        } catch (InvalidExitValueException e) {
            log.debug("Command error code: " + e.getExitValue());
            log.debug("Command error message: " + e.getResult().outputString());
            log.error(e);
            throw new CommandFailureException("Command execution failed with non zero exit code, ", e);
        } catch (InterruptedException | IOException | TimeoutException e) {
            log.error(e);
            throw new CommandFailureException("Error encountered in executing command, ", e);
        }
        log.debug("Command output: " + output);
        ReleaseStatusResponseDto response = new ReleaseStatusResponseDto(output);
        return response;
    }

    @Override
    public List<ListAllChartsResponseDto> listAllCharts() throws CommandFailureException {
        String command = "helm list -o json";
        String output;
        try {
            output = new ProcessExecutor().commandSplit(command)
                    .readOutput(true)
                    .exitValues(0) //valid exit codes, if other code is encountered exception will be raised.
                    .execute().outputString();
        } catch (InvalidExitValueException e) {
            log.debug("Command error code: " + e.getExitValue());
            log.debug("Command error message: " + e.getResult().outputString());
            log.error(e);
            throw new CommandFailureException("Command execution failed with non zero exit code, ", e);
        } catch (InterruptedException | IOException | TimeoutException e) {
            log.error(e);
            throw new CommandFailureException("Error encountered in executing command, ", e);
        }
        log.debug("Command output: " + output);

        List<ListAllChartsResponseDto> response = new ArrayList<>();
        JSONArray outputJson = new JSONArray(output);
        for (Object val : outputJson) {
            response.add(new ListAllChartsResponseDto(val.toString()));
        }
        return response;
    }

    @Override
    public void uninstallChart(String releaseName, String namespace, String kubeConfig) throws CommandFailureException {
        String kubeConfigPath = "";
        try {
            kubeConfigPath = createTempKubeconfig(kubeConfig);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Map<String, String> args = new HashMap<>();
        args.put("releaseName", releaseName);
        args.put("namespace", namespace);
        args.put("kubeConfigPath", kubeConfigPath);
        StrSubstitutor sub = new StrSubstitutor(args, "${", "}");
        String command = sub.replace("helm uninstall ${releaseName} --namespace=${namespace} --kubeconfig=${kubeConfigPath}"); //-o json is not supported in this.
        String output = "";
        try {
            output = new ProcessExecutor().commandSplit(command)
                    .readOutput(true)
                    .exitValues(0) //valid exit codes, if other code is encountered exception will be raised.
                    .execute().outputString();
        } catch (InvalidExitValueException e) {
            log.debug("Command error code: " + e.getExitValue());
            log.debug("Command error message: " + e.getResult().outputString());
            log.error(e);
            throw new CommandFailureException("Command execution failed with non zero exit code, ", e);
        } catch (InterruptedException | IOException | TimeoutException e) {
            log.error(e);
            throw new CommandFailureException("Error encountered in executing command, ", e);
        }
        log.debug("Command output: " + output);
    }

    @Override
    public void rollbackRelease(String releaseName, String revision, String namespace, String kubeConfig) throws CommandFailureException {
        String kubeConfigPath = "";
        try {
            kubeConfigPath = createTempKubeconfig(kubeConfig);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Map<String, String> args = new HashMap<>();
        args.put("releaseName", releaseName);
        args.put("revision", revision);
        args.put("namespace", namespace);
        args.put("kubeConfigPath", kubeConfigPath);
        StrSubstitutor sub = new StrSubstitutor(args, "${", "}");
        String command = sub.replace("helm rollback ${releaseName} ${revision} --wait --namespace=${namespace} --kubeconfig=${kubeConfigPath}"); //-o json is not supported in this.
        String output = "";
        try {
            output = new ProcessExecutor().commandSplit(command)
                    .readOutput(true)
                    .exitValues(0) //valid exit codes, if other code is encountered exception will be raised.
                    .execute().outputString();
        } catch (InvalidExitValueException e) {
            log.debug("Command error code: " + e.getExitValue());
            log.debug("Command error message: " + e.getResult().outputString());
            log.error(e);
            throw new CommandFailureException("Command execution failed with non zero exit code, ", e);
        } catch (InterruptedException | IOException | TimeoutException e) {
            log.error(e);
            throw new CommandFailureException("Error encountered in executing command, ", e);
        }
        log.debug("Command output: " + output);
    }


    private String createTempKubeconfig(String kubeConfig) throws IOException {
        File tmpFile = File.createTempFile("kubeconfig-", null);
        FileWriter writer = new FileWriter(tmpFile);
        writer.write(kubeConfig);
        writer.close();
        return tmpFile.getAbsolutePath();
    }
}
