package org.zigmoi.ketchup.deployment.services;

public enum DeploymentsStatus {

    INITIALISED("initialised"),
    ;

    private String type;

    DeploymentsStatus(String type) {
        this.type = type;
    }

    public String[] getAll() {
        return new String[]{
                "initialised",
        };
    }

    @Override
    public String toString() {
        return type;
    }
}
