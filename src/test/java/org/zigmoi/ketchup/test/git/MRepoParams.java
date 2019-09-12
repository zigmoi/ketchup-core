package org.zigmoi.ketchup.test.git;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class MRepoParams {
    private String status;
    private String repoName;
    private String remoteUrl;
    private boolean existsLocally = false;
    private File gitDir;
    private File pomDir;
    private String repoType;
    private Map<String, MException> exceptions = new HashMap<>();
    private Map<String, Boolean> activities = new HashMap<>();

    public Map<String, Boolean> getActivities() {
        return activities;
    }

    public void setActivities(Map<String, Boolean> activities) {
        this.activities = activities;
    }

    public Map<String, MException> getExceptions() {
        return exceptions;
    }

    public void setExceptions(Map<String, MException> exceptions) {
        this.exceptions = exceptions;
    }

    public File getPomDir() {
        return pomDir;
    }

    public void setPomDir(File pomDir) {
        this.pomDir = pomDir;
    }

    public File getGitDir() {
        return gitDir;
    }

    public void setGitDir(File gitDir) {
        this.gitDir = gitDir;
    }

    public String getRepoType() {
        return repoType;
    }

    public void setRepoType(String repoType) {
        this.repoType = repoType;
    }

    public boolean isExistsLocally() {
        return existsLocally;
    }

    public void setExistsLocally(boolean existsLocally) {
        this.existsLocally = existsLocally;
    }

    public String isRepoType() {
        return repoType;
    }

    public boolean doesExistLocally() {
        return existsLocally;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRepoName() {
        return repoName;
    }

    public void setRepoName(String repoName) {
        this.repoName = repoName;
    }

    public String getRemoteUrl() {
        return remoteUrl;
    }

    public void setRemoteUrl(String remoteUrl) {
        this.remoteUrl = remoteUrl;
    }
}
