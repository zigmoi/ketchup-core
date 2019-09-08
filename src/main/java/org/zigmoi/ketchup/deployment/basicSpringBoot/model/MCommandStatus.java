package org.zigmoi.ketchup.deployment.basicSpringBoot.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class MCommandStatus {

    private boolean successful;
    private List<String> logs = new ArrayList<>();

    public MCommandStatus addLog(String log) {
        logs.add(log);
        return this;
    }
}
