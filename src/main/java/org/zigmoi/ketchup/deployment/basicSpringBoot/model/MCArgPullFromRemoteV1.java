package org.zigmoi.ketchup.deployment.basicSpringBoot.model;

// MCArg -> Model Command Arg

import lombok.Data;

import java.io.File;

@Data
public class MCArgPullFromRemoteV1 {

    private String gitVendor, basePath, repoName;
    private MCArgPullFromRemoteV1.GitVendorArg gitVendorArg;

    public File getRepoPath() {
        return new File(basePath + File.separator + repoName);
    }

    public MCArgPullFromRemoteV1 basePath(String basePath) {
        this.basePath = basePath;
        return this;
    }

    public MCArgPullFromRemoteV1 repoName(String repoName) {
        this.repoName = repoName;
        return this;
    }

    public MCArgPullFromRemoteV1 gitVendor(String gitVendor) {
        this.gitVendor = gitVendor;
        return this;
    }

    public MCArgPullFromRemoteV1 gitVendorArg(MCArgPullFromRemoteV1.GitVendorArg gitVendorArg) {
        this.gitVendorArg = gitVendorArg;
        return this;
    }

    @Data
    public static class GitVendorArg {

        private String url, username, password;

        public GitVendorArg url(String url) {
            this.url = url;
            return this;
        }

        public GitVendorArg username(String username) {
            this.username = username;
            return this;
        }

        public GitVendorArg password(String password) {
            this.password = password;
            return this;
        }
    }
}
