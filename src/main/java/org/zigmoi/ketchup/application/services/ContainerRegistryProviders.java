package org.zigmoi.ketchup.application.services;

public enum ContainerRegistryProviders {

    AWS_ECR("aws-ecr"),
    ;

    private String type;

    ContainerRegistryProviders(String type) {
        this.type = type;
    }

    public String[] getAll() {
        return new String[]{
                "aws-ecr",
        };
    }

    @Override
    public String toString() {
        return type;
    }
}
