package org.zigmoi.ketchup.release.services;

public enum CloudProviders {

    AWS("aws"),
    ;

    private String type;

    CloudProviders(String type) {
        this.type = type;
    }

    public String[] getAll() {
        return new String[]{
                "aws",
        };
    }

    @Override
    public String toString() {
        return type;
    }
}
