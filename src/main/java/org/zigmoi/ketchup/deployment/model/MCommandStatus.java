package org.zigmoi.ketchup.deployment.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class MCommandStatus {

    private boolean successful;
    private List<String> logs;

    public void addLogs(String log) {
        if (logs == null) {
            logs = new ArrayList<>();
        }
        logs.add(log);
    }
}
