package org.zigmoi.ketchup.common;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.AuthResponse;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.command.BuildImageResultCallback;
import com.github.dockerjava.core.command.PullImageResultCallback;
import com.github.dockerjava.core.command.PushImageResultCallback;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Slf4j
public class DockerUtility {

    public static void pullImage(String registryUrl, String registryUser, String registryPass,
                                 String imageRepo, String imageTag) throws InterruptedException, IOException {

        DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withDockerHost("unix:///var/run/docker.sock")
                .withRegistryUrl(registryUrl)
                .withRegistryUsername(registryUser)
                .withRegistryPassword(registryPass)
                .build();
        DockerClient dockerClient = null;
        try {
            dockerClient = DockerClientBuilder.getInstance(config).build();

            AuthResponse response = dockerClient.authCmd().exec();
            log.info(response.getStatus());

            dockerClient.pullImageCmd(imageRepo)
                    .withTag(imageTag)
                    .exec(new PullImageResultCallback())
                    .awaitCompletion();
            log.info("Repo : {} successfully pulled", imageRepo);
        } finally {
            Objects.requireNonNull(dockerClient).close();
        }
    }

    public static void buildAndPushImage(String registryUrl, String registryUser, String registryPass,
                                         String tag, File baseDirectory, File dockerFilePath) throws IOException, InterruptedException {

        DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withDockerHost("unix:///var/run/docker.sock")
                .withRegistryUrl(registryUrl)
                .withRegistryUsername(registryUser)
                .withRegistryPassword(registryPass)
                .build();
        DockerClient dockerClient = null;
        try {
            dockerClient = DockerClientBuilder.getInstance(config).build();
            AuthResponse response = dockerClient.authCmd().exec();
            log.info(response.getStatus());

            Set<String> tags = new HashSet<>();
            tags.add(tag);
            String imageId = dockerClient.buildImageCmd()
                    .withBaseDirectory(baseDirectory)
                    .withDockerfile(dockerFilePath)
                    .withTags(tags)
                    .exec(new BuildImageResultCallback())
                    .awaitImageId();
            log.info("Image : {} successfully build for repo {} : ", imageId, tag);

            dockerClient.pushImageCmd(tag)
                    .exec(new PushImageResultCallback())
                    .awaitCompletion();

            log.info("Image pushed for repo : {}", tag);
        } finally {
            Objects.requireNonNull(dockerClient).close();
        }
    }
}
