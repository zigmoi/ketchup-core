package org.zigmoi.ketchup.project.services;

public enum ProjectSettingsType {

    BUILD_TOOL("build-tool"),
    CLOUD_PROVIDER("cloud-provider"),
    CONTAINER_REGISTRY("container-registry"),
    GIT_PROVIDER("git-provider"),
    HOSTNAME_IP_MAPPING("hostname-ip-mapping"),
    KUBERNETES_CLUSTER("kubernetes-cluster"),
    ;

    private String type;

    ProjectSettingsType(String type) {
        this.type = type;
    }

    public String[] getAll() {
        return new String[]{
                "build-tool",
                "cloud-provider",
                "container-registry",
                "git-provider",
                "hostname-ip-mapping",
                "kubernetes-cluster",
        };
    }

    @Override
    public String toString() {
        return type;
    }
}
