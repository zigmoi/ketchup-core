package org.zigmoi.ketchup.helm.services;

import org.zigmoi.ketchup.helm.dtos.ChartInstallResponseDto;
import org.zigmoi.ketchup.helm.dtos.ListAllChartsResponseDto;
import org.zigmoi.ketchup.helm.dtos.ReleaseStatusResponseDto;

import java.util.List;

public interface HelmService {

    String createChart();

    ChartInstallResponseDto installChart(String chartName, String releaseName, String valuesYamlLocation);

    ChartInstallResponseDto installChart(String chartName, String releaseName);

    ChartInstallResponseDto installChart(String chartName);

    ReleaseStatusResponseDto getReleaseStatus(String releaseName);

    List<ListAllChartsResponseDto> listAllCharts();

    void deleteRelease(String releaseName, String kubeConfig);
}
