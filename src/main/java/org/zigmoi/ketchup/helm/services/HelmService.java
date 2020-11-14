package org.zigmoi.ketchup.helm.services;

import org.zigmoi.ketchup.helm.dtos.ChartInstallResponseDto;
import org.zigmoi.ketchup.helm.dtos.ListAllChartsResponseDto;
import org.zigmoi.ketchup.helm.dtos.ReleaseStatusResponseDto;
import org.zigmoi.ketchup.helm.exceptions.CommandFailureException;

import java.util.List;

public interface HelmService {

    String createChart();

    ChartInstallResponseDto installChart(String chartName, String releaseName, String valuesYamlLocation) throws CommandFailureException;

    ChartInstallResponseDto installChart(String chartName, String releaseName) throws CommandFailureException;

    ChartInstallResponseDto installChart(String chartName) throws CommandFailureException;

    ReleaseStatusResponseDto getReleaseStatus(String releaseName, String namespace, String kubeConfig) throws CommandFailureException;

    List<ListAllChartsResponseDto> listAllCharts() throws CommandFailureException;

    void uninstallChart(String releaseName, String namespace, String kubeConfig) throws CommandFailureException;

    void rollbackRelease(String releaseName, String revision, String namespace, String kubeConfig) throws CommandFailureException;
}
