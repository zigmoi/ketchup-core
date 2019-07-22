package org.zigmoi.ketchup.test.git;

import org.apache.maven.shared.invoker.*;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zigmoi.ketchup.common.ConfigUtility;
import org.zigmoi.ketchup.common.FileUtility;
import sun.misc.BASE64Encoder;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.util.*;

public class GItUrlsExtractor {
    private final static Logger logger = LoggerFactory.getLogger(GItUrlsExtractor.class);
    private static final String BITBUCKET_URL = "https://api.bitbucket.org/1.0/user/repositories";
    private static final String BITBUCKET_USERNAME = "urishav";
    private static final String BITBUCKET_PASSWORD = "";

    // commands
    private static final String MVN_CLEAN_INSTALL = "mvn clean install";
    private static final String MVN_DEPLOY = "mvn deploy";
    private static final String PUSH = "push";
    private static final String PULL = "pull";
    private static final String CLONE = "clone";
    private static final String UPDATE_POM = "update_pom";


    private Map<String, List<String>> excludeList = new HashMap<>();
    private Map<String, List<String>> includeList = new HashMap<>();

    private static GitUtils gitUtils;

    public static void main(String[] args) throws IOException, ParseException, GitAPIException, MavenInvocationException, JSONException {
        GItUrlsExtractor gitUrlsExtractor = new GItUrlsExtractor();
        gitUtils = GitUtils.instance("urishav", "k10bitbucket");
        String reposGitInfoPath = ConfigUtility.instance().getPath("data", "out", "pomupdater", "repos_git_info.json");
        String reposSpreadsheetInfoPath = ConfigUtility.instance().getPath("data", "in", "pomupdater", "repos_spreadsheet_info.json");

        String urlPrefix = "https://urishav@bitbucket.org/";
        String urlPostfix = ".git";
        Map<String, MRepoParams> repos = gitUrlsExtractor.saveAccessibleReposInfoAndGetRemoteUrls(reposGitInfoPath,
                urlPrefix, urlPostfix);

//        Map<String, MRepoParams> repos = gitUrlsExtractor.getReposInfo(reposGitInfoPath, urlPrefix, urlPostfix);
        String repoBasePath = "D:\\NewIdeaProjects";
        List<String> repoBasePaths = Arrays.asList(
//                "C:\\Users\\tapo\\IdeaProjects", "D:\\IdeaProjects",
                repoBasePath
        );
        MDistributionManagementParams distributionManagementTag = gitUrlsExtractor.getDistributionManagementModel(
                "central", "gamma-releases", "http://107.170.134.129:7032/artifactory/gamma",
                "snapshots", "gamma-snapshots", "http://107.170.134.129:7032/artifactory/gamma"
        );
        gitUrlsExtractor.excludeCommandsForSpecifiedRepos();
        gitUrlsExtractor.includeCommandsForSpecifiedRepos();
        gitUrlsExtractor.initializeAllActivities(repos);
        gitUrlsExtractor.scanDirectories(repos, repoBasePaths);
        gitUrlsExtractor.pullOrCloneRepositories(repoBasePath, repos);
        gitUrlsExtractor.segregateRepos(repos, reposSpreadsheetInfoPath);
        gitUrlsExtractor.executeMavenCommands(repos);
        gitUrlsExtractor.printReport(repos);
    }

    private boolean isReportRequired(MRepoParams mRepoParams) {
        if (wereAnyCommandsIntended(mRepoParams)) {
            return true;
        }
        if (mRepoParams.getExceptions().size() > 0) {
            return true;
        }
        if (wereAnyActivitiesDone(mRepoParams.getActivities())) {
            return true;
        }
        return false;
    }

    private boolean wereAnyCommandsIntended(MRepoParams mRepoParams) {
        List<String> commands = Arrays.asList(MVN_CLEAN_INSTALL, MVN_DEPLOY, PULL, PUSH, CLONE, UPDATE_POM);
        for (String command : commands) {
            if (shouldCommandBeExecuted(command, mRepoParams)) {
                return true;
            }
        }
        return false;
    }

    private boolean wereAnyActivitiesDone(Map<String, Boolean> activities) {
        for (Map.Entry<String, Boolean> commandStats : activities.entrySet()) {
            boolean activityStatus = commandStats.getValue();
            if (activityStatus) {
                return true;
            }
        }
        return false;
    }

    private void includeCommandsForSpecifiedRepos() {
        includeList.put(PULL, Arrays.asList("finepoint-analytics"));
        includeList.put(PUSH, Arrays.asList("finepoint-analytics"));
        includeList.put(CLONE, Arrays.asList("finepoint-analytics"));
        includeList.put(UPDATE_POM, Arrays.asList("finepoint-analytics"));
        includeList.put(MVN_CLEAN_INSTALL, Arrays.asList("finepoint-analytics"));
        includeList.put(MVN_DEPLOY, Arrays.asList("finepoint-analytics"));
    }

    private void excludeCommandsForSpecifiedRepos() {
        excludeList.put(PULL, Arrays.asList("skybase-zain-south-sudan"));
        excludeList.put(PUSH, Arrays.asList("skybase-zain-south-sudan"));
        excludeList.put(CLONE, Arrays.asList("skybase-zain-south-sudan"));
        excludeList.put(UPDATE_POM, Arrays.asList("skybase-zain-south-sudan"));
        excludeList.put(MVN_CLEAN_INSTALL, Arrays.asList("spring-boot-sso", "skybase-zain-south-sudan"));
        excludeList.put(MVN_DEPLOY, Arrays.asList("spring-boot-sso", "skybase-zain-south-sudan"));
    }

    private void initializeAllActivities(Map<String, MRepoParams> repos) {
        for (Map.Entry<String, MRepoParams> repo : repos.entrySet()) {
            MRepoParams repoParams = repo.getValue();
            updateActivities(repoParams, MVN_CLEAN_INSTALL, false);
            updateActivities(repoParams, MVN_DEPLOY, false);
            updateActivities(repoParams, PUSH, false);
            updateActivities(repoParams, PULL, false);
            updateActivities(repoParams, CLONE, false);
        }
    }

    private void pushFiles(Map<String, MRepoParams> repos) {
        repos.entrySet().parallelStream().forEach(repo -> {
            MRepoParams repoParams = repo.getValue();
            if (shouldCommandBeExecuted(PUSH, repoParams)) {
                try {
                    Map<String, Boolean> activies = repoParams.getActivities();
                    if (activies.get(MVN_CLEAN_INSTALL)) {
                        logger.info("Pushing " + repoParams.getRepoName());
                        gitUtils.push(repoParams.getGitDir(), "pom.xml");
                        logger.info("Push successful " + repoParams.getRepoName());
                        updateActivities(repoParams, PUSH, true);
                    }
                } catch (Exception e) {
                    logger.error("Push failed " + repoParams.getRepoName(), e);
                }
            }
        });
    }

    private Map<String, MRepoParams> saveAccessibleReposInfoAndGetRemoteUrls(String destination,
                                                                             String urlPrefix,
                                                                             String urlPostfix) throws IOException, ParseException, JSONException {
        logger.info("Fetching repositories info from " + BITBUCKET_URL + " ....");
        URL url = new URL(BITBUCKET_URL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        BASE64Encoder encoder = new BASE64Encoder();
        String encoded = encoder.encode((BITBUCKET_USERNAME + ":" + BITBUCKET_PASSWORD).getBytes("UTF-8"));
        connection.setRequestProperty("Authorization", "Basic " + encoded);
        connection.setRequestMethod("GET");
        connection.connect();
        int responseCode = connection.getResponseCode();
        BufferedReader in = new BufferedReader(
                new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        logger.info("Fetched repositories info from " + BITBUCKET_URL);
        JSONArray repos = new JSONArray(response.toString());
        logger.info("Saving repositories info to " + destination);
        File file = new File(destination);
        FileUtility.createAndWrite(file, response.toString());
        logger.info("Saved repositories info in " + destination);
        return getReposInfo(repos, urlPrefix, urlPostfix);
    }

    private void executeMavenCommands(Map<String, MRepoParams> repos) throws MavenInvocationException {
        repos.entrySet().parallelStream().forEach(this::executeCommand);
        if (!areAllCommandsExecuted(repos)) {
            executeRemainingMavenCommands(repos);
        }
    }

    private void executeCommand(Map.Entry<String, MRepoParams> repo) {
        MRepoParams mRepoParams = repo.getValue();
        File pomFile = mRepoParams.getPomDir();
        InvocationRequest request = new DefaultInvocationRequest();
        request.setPomFile(pomFile);
        if (shouldCommandBeExecuted(MVN_CLEAN_INSTALL, mRepoParams)) {
            if (mRepoParams.getRepoType().equals("plugin")) {
                logger.info("Executing mvn clean install for " + pomFile.getPath());
                request.setGoals(Arrays.asList("clean", "install"));
                Invoker invoker = new DefaultInvoker();
                InvocationResult result = null;
                try {
                    result = invoker.execute(request);
                    updateActivities(mRepoParams, MVN_CLEAN_INSTALL, true);
                } catch (MavenInvocationException e) {
                    logger.error("mvn clean install for " + pomFile.getPath() + " failed");
                    e.printStackTrace();
                }
                int exitCode = result.getExitCode();
                if (exitCode != 0) {
                    logger.error("mvn clean install for " + pomFile.getPath() + " failed");
                    logException("mvm clean install", mRepoParams, result);
                } else {
                    if (shouldCommandBeExecuted(MVN_DEPLOY, mRepoParams)) {
                        logger.info("Executing mvn deploy for " + pomFile.getPath());
                        request.setGoals(Arrays.asList("deploy"));
                        invoker = new DefaultInvoker();
                        try {
                            result = invoker.execute(request);
                            updateActivities(mRepoParams, MVN_DEPLOY, true);
                        } catch (MavenInvocationException e) {
                            logger.error("mvn deploy for " + pomFile.getPath() + " failed");
                            e.printStackTrace();
                        }
                        exitCode = result.getExitCode();
                        if (exitCode != 0) {
                            logger.error("mvn deploy for " + pomFile.getPath() + " failed");
                            logException("mvm deploy", mRepoParams, result);
                        }
                    }
                }
            } else if (mRepoParams.getRepoType().equals("application")) {
                logger.info("Executing mvn clean install for " + pomFile.getPath());
                request.setGoals(Arrays.asList("clean", "install"));
                Invoker invoker = new DefaultInvoker();
                InvocationResult result = null;
                try {
                    result = invoker.execute(request);
                    updateActivities(mRepoParams, MVN_CLEAN_INSTALL, true);
                } catch (MavenInvocationException e) {
                    logger.error("mvn clean install for " + pomFile.getPath() + " failed");
                    e.printStackTrace();
                }
                int exitCode = result.getExitCode();
                if (exitCode != 0) {
                    logger.error("mvn clean install for " + pomFile.getPath() + " failed");
                    logException("mvm clean install", mRepoParams, result);
                }
            }
        }
    }

    private boolean shouldCommandBeExecuted(String command, MRepoParams mRepoParams) {
        List<String> excludedRepos = excludeList.get(command);
        List<String> includedRepos = includeList.get(command);
        String repoName = mRepoParams.getRepoName();
        boolean isIncluded = false;
        boolean isExcluded = false;

        for (String includedRepo : includedRepos) {
            if (repoName.contains(includedRepo) && !includedRepo.isEmpty()) {
                isIncluded = true;
            }
        }
        for (String excludedRepo : excludedRepos) {
            if (repoName.contains(excludedRepo) && !excludedRepo.isEmpty()) {
                isExcluded = true;
            }
        }
        return isIncluded && !isExcluded;
    }

    private void executeRemainingMavenCommands(Map<String, MRepoParams> repos) throws MavenInvocationException {
        while (!areAllCommandsExecuted(repos)) {
            repos.entrySet().parallelStream().forEach(repo -> {
                if (repo.getValue().getExceptions().size() > 0) {
                    executeCommand(repo);
                }
            });
        }
    }

    private boolean areAllCommandsExecuted(Map<String, MRepoParams> repos) {
        for (Map.Entry<String, MRepoParams> repo : repos.entrySet()) {
            if (repo.getValue().getExceptions().size() > 0) {
                return false;
            }
        }
        return true;
    }

    private void logException(String command, MRepoParams mRepoParams, InvocationResult result) {
        MException exception = new MException();
        exception.setErrorCode(result.getExitCode());
        exception.setException(result.getExecutionException());
        Map<String, MException> exceptions = mRepoParams.getExceptions();
        exceptions.put(command, exception);
    }

    private void updateActivities(MRepoParams repoParams, String command, boolean status) {
        Map<String, Boolean> activities = repoParams.getActivities();
        activities.put(command, status);
    }

    private MDistributionManagementParams getDistributionManagementModel(String repositoryId,
                                                                         String repositoryName,
                                                                         String repositoryUrl,
                                                                         String snapshotRepositoryId,
                                                                         String snapshotRepositoryName,
                                                                         String snapshotRepositoryUrl) {
        MDistributionManagementParams mDistributionManagementParams = new MDistributionManagementParams();
        mDistributionManagementParams.setRepositoryId(repositoryId);
        mDistributionManagementParams.setRepositoryName(repositoryName);
        mDistributionManagementParams.setRepositoryUrl(repositoryUrl);
        mDistributionManagementParams.setSnapshotRepositoryId(snapshotRepositoryId);
        mDistributionManagementParams.setSnapshotRepositoryName(snapshotRepositoryName);
        mDistributionManagementParams.setSnapshotRepositoryUrl(snapshotRepositoryUrl);
        return mDistributionManagementParams;
    }

    public void segregateRepos(Map<String, MRepoParams> repos, String path) throws IOException, JSONException {
        logger.info("Segregating repositories between application and plugins ...");
        String jsonFileContent = FileUtility.readDataFromFile(new File(path));
        JSONArray reposSpreadSheetContent = new JSONArray(jsonFileContent);

        for (int i = 0; i < reposSpreadSheetContent.length(); i++) {
            JSONObject repoObjects = (JSONObject) reposSpreadSheetContent.get(i);
            for (Iterator it = repoObjects.keys(); it.hasNext(); ) {
                Object repoUrl = it.next();
                String remoteUrl = (String) repoUrl;
                JSONObject repoDetails = (JSONObject) repoObjects.get((String) repoUrl);
                String repoCategoryFromJson = ((String) repoDetails.get("Application / Plugin"));
                if (repoCategoryFromJson.equalsIgnoreCase("a")) {
                    repos.get(remoteUrl).setRepoType("application");
                } else if (repoCategoryFromJson.equalsIgnoreCase("p")) {
                    repos.get(remoteUrl).setRepoType("plugin");
                } else {
                    repos.get(remoteUrl).setRepoType("unknown");
                }
            }
        }
        logger.info("Segregation successfully done.");
    }

    private void pullOrCloneRepositories(String repoBasePath, Map<String, MRepoParams> repos) throws IOException, GitAPIException {
        repos.entrySet().
                parallelStream().forEach(repo -> {
                    MRepoParams repoParams = repo.getValue();
                    if (!repoParams.doesExistLocally()) {
                        if (shouldCommandBeExecuted(CLONE, repoParams)) {
                            String newRepoPath = repoBasePath + File.separator + repoParams.getRepoName();
                            try {
                                FileUtility.deleteDirectory(new File(newRepoPath));
                            } catch (IOException e) {
                                logger.error(e.getMessage(), e);
                            }
                            boolean dirReturnCode = new File(newRepoPath).mkdir();
                            logger.info("Cloning " + repoParams.getRepoName() + " ...");
                            try {
                                gitUtils.clone(repoParams.getRemoteUrl(), newRepoPath);
                                updateActivities(repoParams, CLONE, true);
                            } catch (IOException | GitAPIException e) {
                                logger.error("Cloning failed" + repoParams.getRepoName() + " to " + repoBasePath);
                            }
                            logger.info("Successfully Cloned " + repoParams.getRepoName() + " to " + repoBasePath);
                        }
                    } else {
                        if (shouldCommandBeExecuted(PULL, repoParams)) {
                            logger.info("Pulling " + repoParams.getRepoName() + " ...");
                            try {
                                gitUtils.pull(repoParams.getGitDir());
                                updateActivities(repoParams, PULL, true);
                            } catch (IOException | GitAPIException e) {
                                logger.error("Pull failed" + repoParams.getRepoName() + " to " + repoBasePath);
                            }
                            logger.info("Successfully Pulled " + repoParams.getRepoName() + " to " + repoBasePath);
                        }
                    }
                }
        );
    }

    private static void printMap(Map<String, MRepoParams> repos) {
        for (Map.Entry<String, MRepoParams> repo : repos.entrySet()) {
        }
    }

    private void scanDirectories(Map<String, MRepoParams> repoParamsMap,
                                 List<String> repoBasePaths) throws IOException {
        for (String repoBasePath : repoBasePaths) {
            logger.info("Scanning .git of local repositories in " + repoBasePath + " ...");
            List<File> files = FileUtility.listOnlyDirs(new File(repoBasePath), true);
            List<File> filteredFiles = getFilesOfSpecifiedFormat(files, ".git");
            Map<String, File> gitRemoteUrlsFromLocal = gitUtils.getGitRemoteUrlsFromLocal(filteredFiles);
            updateLocalExistence(gitRemoteUrlsFromLocal, repoParamsMap);
            logger.info("Scanned .git of local repositories in " + repoBasePath);
        }
    }

    private void updateLocalExistence(Map<String, File> gitRemoteUrlsFromLocal, Map<String, MRepoParams> repoParamsMap) {
        Collection<String> remoteUrlsFromLocal = gitRemoteUrlsFromLocal.keySet();
        for (Map.Entry<String, MRepoParams> repoParamsEntry : repoParamsMap.entrySet()) {
            MRepoParams mRepoParams = repoParamsEntry.getValue();
            boolean existsInLocal = mRepoParams.doesExistLocally();
            String remoteUrl = mRepoParams.getRemoteUrl();
            if (!existsInLocal && remoteUrlsFromLocal.contains(remoteUrl)) {
                mRepoParams.setExistsLocally(true);
                mRepoParams.setGitDir(gitRemoteUrlsFromLocal.get(remoteUrl));
            }
        }
    }

    private List<File> getFilesOfSpecifiedFormat(List<File> files, String format) {
        List<File> filteredFiles = new ArrayList<>();
        for (File file : files) {
            if (file.getPath().endsWith(format)) {
                filteredFiles.add(file);
            }
        }
        return filteredFiles;
    }

    private File getFileOfSpecifiedNameAndFormat(List<File> files, String fileNameAndFormat) {
        for (File file : files) {
            if (file.getName().equals(fileNameAndFormat)) {
                return file;
            }
        }
        return null;
    }

    private Map<String, MRepoParams> getReposInfo(String filePath,
                                                  String urlPrefix,
                                                  String urlPostfix) throws IOException, ParseException, JSONException {
        logger.info("Loading repositories info from " + filePath);
        Map<String, MRepoParams> reposInfo = new HashMap<>();
        String jsonFileContent = FileUtility.readDataFromFile(new File(filePath));
        JSONArray repos = new JSONArray(jsonFileContent);
        for (int i = 0; i < repos.length(); i++) {
            MRepoParams mRepoParams = new MRepoParams();
            JSONObject jsonRepo = (JSONObject) repos.get(i);
            String repoName = jsonRepo.getString("slug");
            String repoOwner = jsonRepo.getString("owner");
            String remoteUrl = urlPrefix + repoOwner + "/" + repoName + urlPostfix;
            mRepoParams.setRepoName(repoName);
            mRepoParams.setRemoteUrl(remoteUrl);
            reposInfo.put(remoteUrl, mRepoParams);
        }
        logger.info("Successfully loaded repositories info from " + filePath);
        return reposInfo;
    }

    private Map<String, MRepoParams> getReposInfo(JSONArray repos,
                                                  String urlPrefix,
                                                  String urlPostfix) throws IOException, ParseException, JSONException {
        Map<String, MRepoParams> reposInfo = new HashMap<>();
        for (int i = 0; i < repos.length(); i++) {
            MRepoParams mRepoParams = new MRepoParams();
            JSONObject jsonRepo = (JSONObject) repos.get(i);
            String repoName = jsonRepo.getString("slug");
            String repoOwner = jsonRepo.getString("owner");
            String remoteUrl = urlPrefix + repoOwner + "/" + repoName + urlPostfix;
            mRepoParams.setRepoName(repoName);
            mRepoParams.setRemoteUrl(remoteUrl);
            reposInfo.put(remoteUrl, mRepoParams);
        }
        return reposInfo;
    }

    private void printReport(Map<String, MRepoParams> repos) {
        System.out.println();
        System.out.println();
        System.out.println("------------------------------------------------------------------------- REPORT " +
                "----------------------------------------------------------------------------------------");
        System.out.printf("%20s %20s %20s %20s %20s %20s %20s", "REPO NAME", "PULL", "CLONE", "POM UPDATE",
                "CLEAN INSTALL", "DEPLOY", "PUSH");
        System.out.println();
        System.out.println("-------------------------------------------------------------------------------" +
                "------------------------------------------------------------------------------------------");
        for (Map.Entry<String, MRepoParams> repo : repos.entrySet()) {
            MRepoParams mRepoParams = repo.getValue();
            Map<String, Boolean> activities = mRepoParams.getActivities();
            if (isReportRequired(mRepoParams)) {
                System.out.format("%20s %20s %20s %20s %20s %20s %20s",
                        mRepoParams.getRepoName(),
                        activities.get(PULL) ? "Done" : "Not Done",
                        activities.get(CLONE) ? "Done" : "Not Done",
                        activities.get(UPDATE_POM) ? "Done" : "Not Done",
                        activities.get(MVN_CLEAN_INSTALL) ? "Done" : "Not Done",
                        activities.get(MVN_DEPLOY) ? "Done" : "Not Done",
                        activities.get(PUSH) ? "Done" : "Not Done");
                System.out.println();
            }
        }
        System.out.println("-------------------------------------------------------------------------------" +
                "------------------------------------------------------------------------------------------");
    }
}
