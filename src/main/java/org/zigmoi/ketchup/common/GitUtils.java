package org.zigmoi.ketchup.common;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.lib.Config;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GitUtils {

    private String GIT_USERNAME;
    private String GIT_PASSWORD;
    private static GitUtils gitUtils;

    private String[] ignoreCommitMessageWords = new String[]{
            "--", "-", "#", "_", "__", "+", "++", ">", "-->", "*", "|"
            , "chore:", "chore"
            , "test:", "test"
            , "enhancement:", "enhancement"
            , "bug:", "bug"
    };

    public static GitUtils instance(String userName, String password) {
        if (gitUtils == null) {
            gitUtils = new GitUtils(userName, password);
        }
        return gitUtils;
    }

    private GitUtils(String userName, String password) {
        this.GIT_USERNAME = userName;
        this.GIT_PASSWORD = password;
    }

    public Map<String, File> getGitRemoteUrlsFromLocal(List<File> gitFiles) throws IOException {
        Map<String, File> gitRemoteUrls = new HashMap<>();
        for (File gitFile : gitFiles) {
            Git git = Git.open(gitFile);
            Repository repo = git.getRepository();
            Config config = repo.getConfig();
            gitRemoteUrls.put(config.getString("remote", "origin", "url"), gitFile);
        }
        return gitRemoteUrls;
    }

    public void clone(String remoteUrl, String repoBasePath) throws IOException, GitAPIException {
        File localPath = new File(repoBasePath);
        try (Git result = Git.cloneRepository()
                .setCredentialsProvider(new UsernamePasswordCredentialsProvider(GIT_USERNAME, GIT_PASSWORD))
                .setURI(remoteUrl)
                .setDirectory(localPath)
                .call()) {
        }
    }

    public void pull(File gitFile) throws IOException, GitAPIException {
        Git git = Git.open(gitFile);
        git
                .pull()
                .setCredentialsProvider(new UsernamePasswordCredentialsProvider(GIT_USERNAME, GIT_PASSWORD))
                .call();
    }

    public void push(File gitFile, String pomFile) throws IOException, GitAPIException {
        Git git = Git.open(gitFile);
        boolean ignore = false;
        try {
            git.commit().setOnly(pomFile).setMessage("Pom file updated").call();
        } catch (JGitInternalException e) {
            if (e.getMessage().contains("No changes")) {
                ignore = true;
            }
        }
        if (!ignore) {
            git.push()
                    .setCredentialsProvider(new UsernamePasswordCredentialsProvider(GIT_USERNAME, GIT_PASSWORD))
                    .call();
        }

    }

    public static void main(String[] args) {

    }
}
