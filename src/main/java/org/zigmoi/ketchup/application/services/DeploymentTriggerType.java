package org.zigmoi.ketchup.application.services;

public enum DeploymentTriggerType {

    GIT_WEBHOOK("GIT WEBHOOK"),
    MANUAL("MANUAL"),
    ;

    private String type;

    DeploymentTriggerType(String type) {
        this.type = type;
    }

    public String[] getAll() {
        return new String[]{
                "GIT WEBHOOK",
                "MANUAL",
        };
    }

    @Override
    public String toString() {
        return type;
    }
}
