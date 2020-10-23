package org.zigmoi.ketchup.project.services;

public enum SettingType {

    BUILD_TOOL("build-tool"),
    CONTAINER_REGISTRY("container-registry"),
    K8S_HOST_ALIAS("k8s-host-alias"),
    KUBERNETES_CLUSTER("kubernetes-cluster"),
    ;

    private String type;

    SettingType(String type) {
        this.type = type;
    }

    public String[] getAll() {
        return new String[]{
                "build-tool",
                "container-registry",
                "k8s-host-alias",
                "kubernetes-cluster",
        };
    }

    @Override
    public String toString() {
        return type;
    }
}
