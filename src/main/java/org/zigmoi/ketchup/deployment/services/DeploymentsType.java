package org.zigmoi.ketchup.deployment.services;

public enum DeploymentsType {

    BASIC_SPRING_BOOT("basic-spring-boot"),
    ;

    private String type;

    DeploymentsType(String type) {
        this.type = type;
    }

    public String[] getAll() {
        return new String[]{
                "basic-spring-boot",
        };
    }

    @Override
    public String toString() {
        return type;
    }
}
