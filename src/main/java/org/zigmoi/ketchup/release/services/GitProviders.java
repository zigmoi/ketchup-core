package org.zigmoi.ketchup.release.services;

public enum GitProviders {

    BITBUCKET("bitbucket"),
    GITLAB("gitlab"),
    ;

    private String type;

    GitProviders(String type) {
        this.type = type;
    }

    public String[] getAll() {
        return new String[]{
                "bitbucket",
                "gitlab",
        };
    }

    @Override
    public String toString() {
        return type;
    }
}
