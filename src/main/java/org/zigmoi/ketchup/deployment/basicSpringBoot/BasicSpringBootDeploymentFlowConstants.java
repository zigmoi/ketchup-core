package org.zigmoi.ketchup.deployment.basicSpringBoot;

public class BasicSpringBootDeploymentFlowConstants {

    // Commands
    public final static String C_PULL_FROM_REMOTE = "pull-from-remote";
    public final static String C_MAVEN_CLEAN_INSTALL = "maven-clean-install";
    public final static String C_BUILD_SPRING_BOOT_DOCKER_IMAGE = "build-spring-boot-docker-image";
    public final static String C_DEPLOY_IN_KUBERNETES = "deploy-in-kubernetes";

    // Template Path
    public final static String TP_MVN_CLEAN_INSTALL = "ketchup.deployment-template.mvn-clean-install";
    public final static String TP_BUILD_SPRING_BOOT_DOCKER_IMAGE = "ketchup.deployment-template.build-basic-spring-boot-docker-image";

    // Docker reigistry vendor
    public final static String DV_AWS_ECR = "aws-ecr";
}
