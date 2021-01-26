package org.zigmoi.ketchup.common;

import org.apache.commons.collections4.CollectionUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.lib.Config;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GitUtility {

    private static GitUtility gitUtility;

    private String[] ignoreCommitMessageWords = new String[]{
            "--", "-", "#", "_", "__", "+", "++", ">", "-->", "*", "|"
            , "chore:", "chore"
            , "test:", "test"
            , "enhancement:", "enhancement"
            , "bug:", "bug"
    };

    public static GitUtility instance() {
        if (gitUtility == null) {
            gitUtility = new GitUtility();
        }
        return gitUtility;
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

    public void clone(String username, String password, String remoteUrl, String repoBasePath) throws IOException, GitAPIException {
        File localPath = new File(repoBasePath);
        try (Git result = Git.cloneRepository()
                .setCredentialsProvider(new UsernamePasswordCredentialsProvider(username, password))
                .setURI(remoteUrl)
                .setDirectory(localPath)
                .call()) {
        }
    }

    public void pull(String userName, String password, File gitFile) throws IOException, GitAPIException {
        Git git = Git.open(gitFile);
        PullResult result = git
                .pull()
                .setCredentialsProvider(new UsernamePasswordCredentialsProvider(userName, password))
                .call();
    }

    public void push(String username, String password, File gitFile, String pomFile) throws IOException, GitAPIException {
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
                    .setCredentialsProvider(new UsernamePasswordCredentialsProvider(username, password))
                    .call();
        }

    }

    public static void main(String[] args) throws GitAPIException {
        System.out.println(GitUtility.instance().testConnection("gitlab-token", "gitlab-token-pwd", "repo-url"));
    }

    public Collection<Ref> lsRemote(String username, String password, String repoURL) throws GitAPIException {
        return Git.lsRemoteRepository()
                .setCredentialsProvider(new UsernamePasswordCredentialsProvider(username, password))
                .setRemote(repoURL)
                .call();
    }

    public boolean testConnection(String username, String password, String repoURL) throws GitAPIException {
        return !CollectionUtils.isEmpty(lsRemote(username, password, repoURL));
    }
}
