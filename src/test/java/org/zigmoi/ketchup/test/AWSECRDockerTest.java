package org.zigmoi.ketchup.test;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.services.ecr.AmazonECR;
import com.amazonaws.services.ecr.AmazonECRClient;
import com.amazonaws.services.ecr.model.*;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.AuthResponse;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.command.BuildImageResultCallback;
import com.github.dockerjava.core.command.PullImageResultCallback;
import com.github.dockerjava.core.command.PushImageResultCallback;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zigmoi.ketchup.common.ConfigUtility;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AWSECRDockerTest {

    private final static Logger logger = LoggerFactory.getLogger(AWSECRDockerTest.class);

    public static void main(String[] args) throws InterruptedException, IOException {
        // ECR client starts
        AmazonECR ecrClient = AmazonECRClient.builder()
                .withCredentials(new AWSCredentialsProvider() {
                    @Override
                    public AWSCredentials getCredentials() {
                        return new AWSCredentials() {
                            @Override
                            public String getAWSAccessKeyId() {
                                return ConfigUtility.instance().getProperty("test.aws-credentials.access-key-id");
                            }

                            @Override
                            public String getAWSSecretKey() {
                                return ConfigUtility.instance().getProperty("test.aws-credentials.secret-access-key");
                            }
                        };
                    }

                    @Override
                    public void refresh() {
                        logger.warn("AWS Key refresh not implemented");
                    }
                })
                .build();

        GetAuthorizationTokenRequest getAuthTokenRequest = new GetAuthorizationTokenRequest();
        List<String> registryIds = new ArrayList<>();
        registryIds.add(ConfigUtility.instance().getProperty("test.aws-credentials.docker-registry-id"));
        getAuthTokenRequest.setRegistryIds(registryIds);

        // Get Authorization Token
        GetAuthorizationTokenResult getAuthTokenResult = ecrClient.getAuthorizationToken(getAuthTokenRequest);
        AuthorizationData authData = getAuthTokenResult.getAuthorizationData().get(0);
        String userPassword = StringUtils.newStringUtf8(Base64.decodeBase64(authData.getAuthorizationToken()));
        String user = userPassword.substring(0, userPassword.indexOf(":"));
        String password = userPassword.substring(userPassword.indexOf(":") + 1);
        String registryUrl = authData.getProxyEndpoint();
        // ECR client ends

        String tag = ConfigUtility.instance().getProperty("test.aws-credentials.docker-registry-id")
                + ".dkr.ecr.us-east-1.amazonaws.com/ovacs/gridmaze-acs:latest";
//        pullImage(registryUrl, user, password, repo, "latest");
        buildImage(registryUrl, user, password, tag);
    }

    public static void pullImage(String registryUrl, String registryUser, String registryPass,
                                 String imageRepo, String imageTag) throws InterruptedException, IOException {

        DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withDockerHost("unix:///var/run/docker.sock")
                .withRegistryUrl(registryUrl)
                .withRegistryUsername(registryUser)
                .withRegistryPassword(registryPass)
                .build();
        DockerClient dockerClient = DockerClientBuilder.getInstance(config).build();

        AuthResponse response = dockerClient.authCmd().exec();
        logger.info(response.getStatus());

        dockerClient.pullImageCmd(imageRepo)
                .withTag(imageTag)
                .exec(new PullImageResultCallback())
                .awaitCompletion();
        logger.info("Repo : {} successfully pulled", imageRepo);

        dockerClient.close();
    }

    public static void buildImage(String registryUrl, String registryUser, String registryPass,
                                 String tag) throws IOException, InterruptedException {

        DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withDockerHost("unix:///var/run/docker.sock")
                .withRegistryUrl(registryUrl)
                .withRegistryUsername(registryUser)
                .withRegistryPassword(registryPass)
                .build();
        DockerClient dockerClient = DockerClientBuilder.getInstance(config).build();

        AuthResponse response = dockerClient.authCmd().exec();
        logger.info(response.getStatus());

        Set<String> tags = new HashSet<>();
        tags.add(tag);
        String imageId = dockerClient.buildImageCmd()
                .withBaseDirectory(new File("/home/tapo/IdeaProjects/gamma-dev/gridmaze/gridmaze-acs"))
                .withDockerfile(new File("/home/tapo/IdeaProjects/gamma-dev/gridmaze/gridmaze-acs/Dockerfile"))
                .withTags(tags)
                .exec(new BuildImageResultCallback())
                .awaitImageId();
        logger.info("Image : {} successfully build for repo : ", imageId, tag);

        dockerClient.pushImageCmd(tag)
                .exec(new PushImageResultCallback())
                .awaitCompletion();

        logger.info("Image pushed for repo : {}", tag);

        dockerClient.close();
    }
}
