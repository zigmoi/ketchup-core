package org.zigmoi.ketchup.application.dtos;

import lombok.Data;

@Data
public class DeploymentStatus {
    private int requiredReplicas;
    private int availableReplicas;
    private int readyReplicas;
    private int uptoDateReplicas;
    private String revisionVersionNo;
    private String status; //Success|In-Progress|Failed
    private String reason;
    private boolean healthy;
}
