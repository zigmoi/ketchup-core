package org.zigmoi.ketchup.deployment.basicSpringBoot.model;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class MCArgBuildSpringBootDockerImageV1 {

    private String basePath, dockerFilePath, dockerFileTemplatePath;
    private Map<String, String> dockerFileTemplateArgs = new HashMap<>();
    private String dockerRegistryVendor, dockerBuildImageName, dockerBuildImageTag;
    private DockerVendorArg dockerVendorArg;

    public MCArgBuildSpringBootDockerImageV1 basePath(String basePath) {
        this.basePath = basePath;
        return this;
    }

    public MCArgBuildSpringBootDockerImageV1 putDockerFileTemplateArg(String key, String val) {
        this.dockerFileTemplateArgs.put(key, val);
        return this;
    }

    public MCArgBuildSpringBootDockerImageV1 dockerFilePath(String dockerFilePath) {
        this.dockerFilePath = dockerFilePath;
        return this;
    }

    public MCArgBuildSpringBootDockerImageV1 dockerVendorArg(DockerVendorArg dockerVendorArg) {
        this.dockerVendorArg = dockerVendorArg;
        return this;
    }

    public MCArgBuildSpringBootDockerImageV1 dockerBuildImageTag(String dockerBuildImageTag) {
        this.dockerBuildImageTag = dockerBuildImageTag;
        return this;
    }

    public MCArgBuildSpringBootDockerImageV1 dockerBuildImageName(String dockerBuildImageName) {
        this.dockerBuildImageName = dockerBuildImageName;
        return this;
    }

    public MCArgBuildSpringBootDockerImageV1 dockerFileTemplatePath(String dockerFileTemplatePath) {
        this.dockerFileTemplatePath = dockerFileTemplatePath;
        return this;
    }

    public MCArgBuildSpringBootDockerImageV1 dockerRegistryVendor(String dockerRegistryVendor) {
        this.dockerRegistryVendor = dockerRegistryVendor;
        return this;
    }

    @Data
    public static class DockerVendorArg {

        private String registryId, repo, registryBaseUrl, awsAccessKeyId, awsSecretKey;

        public DockerVendorArg registryId(String registryId) {
            this.registryId = registryId;
            return this;
        }

        public DockerVendorArg registryBaseUrl(String registryBaseUrl) {
            this.registryBaseUrl = registryBaseUrl;
            return this;
        }

        public DockerVendorArg awsAccessKeyId(String awsAccessKeyId) {
            this.awsAccessKeyId = awsAccessKeyId;
            return this;
        }

        public DockerVendorArg awsSecretKey(String awsSecretKey) {
            this.awsSecretKey = awsSecretKey;
            return this;
        }

        public DockerVendorArg repo(String repo) {
            this.repo = repo;
            return this;
        }
    }
}
