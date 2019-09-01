package org.zigmoi.ketchup.test.git;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.zigmoi.ketchup.common.ConfigUtility;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class SampleCommands {

    private final static String GIT_USERNAME = "btapo";
    private final static String GIT_PASSWORD = "";

    public static void main(String[] args) throws IOException, GitAPIException {
        String repoPath = ConfigUtility.instance().getTmpDir() + File.separator + "git-testrepos";
        run(repoPath);
    }

    private static void run(String repoPath) throws IOException, GitAPIException {
        clone("https://gitlab.com/zigmoi/ketchup/ketchup-demo-basicspringboot.git", repoPath);
        System.out.println(getRemoteUrl(repoPath));
        treeWalk(repoPath);
        pull(repoPath);
        push(repoPath);
    }

    public static String getRemoteUrl(String repoPath) throws IOException {
        Repository repository = new RepositoryBuilder().findGitDir(new File(repoPath)).build();
        Config storedConfig = repository.getConfig();
        Set<String> remotes = storedConfig.getSubsections("remote");
        List<String> items = new ArrayList<>();
        for (String remoteName : remotes) {
            String url = storedConfig.getString("remote", remoteName, "url");
            items.add(remoteName + " " + url);
        }
        return String.join("\n", items);
    }

    public static void treeWalk(String repoPath) throws IOException {
        File gitWorkDir = new File(repoPath);
        Git git = Git.open(gitWorkDir);
        Repository repo = git.getRepository();

        ObjectId lastCommitId = repo.resolve(Constants.HEAD);

        RevWalk revWalk = new RevWalk(repo);
        RevCommit commit = revWalk.parseCommit(lastCommitId);

        RevTree tree = commit.getTree();

        TreeWalk treeWalk = new TreeWalk(repo);
        treeWalk.addTree(tree);
        treeWalk.setRecursive(true);
        while (treeWalk.next()) {
            System.out.println(treeWalk.getPathString());
        }
    }

    public static void pull(String repoPath) throws IOException, GitAPIException {
        File gitWorkDir = new File(repoPath);
        Git git = Git.open(gitWorkDir);
        git.pull()
                .setCredentialsProvider(new UsernamePasswordCredentialsProvider(GIT_USERNAME, GIT_PASSWORD))
                .call();
    }

    public static void push(String repoPath) throws IOException, GitAPIException {
        File gitWorkDir = new File(repoPath);
        Git git = Git.open(gitWorkDir);
        git.push()
                .setCredentialsProvider(new UsernamePasswordCredentialsProvider(GIT_USERNAME, GIT_PASSWORD))
                .call();
    }

    public static void clone(String remoteUrl, String repoBasePath) throws IOException, GitAPIException {
        File localPath = new File(repoBasePath);
        try (Git result = Git.cloneRepository()
                .setCredentialsProvider(new UsernamePasswordCredentialsProvider(GIT_USERNAME, GIT_PASSWORD))
                .setURI(remoteUrl)
                .setDirectory(localPath)
                .call()) {
            System.out.println("Having repository: " + result.getRepository().getDirectory());
        }
    }
}
