package org.zigmoi.ketchup.common;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.services.ecr.AmazonECR;
import com.amazonaws.services.ecr.AmazonECRClient;
import com.amazonaws.services.ecr.model.*;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.StringUtils;
import org.apache.commons.lang3.tuple.Triple;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AWSECRUtility {

    public static Triple<String, String, String> getDockerAccessDetails(String registryId, String accessKeyId, String secretAccessKey) throws InterruptedException, IOException {
        // ECR client starts
        AmazonECR ecrClient = AmazonECRClient.builder()
                .withCredentials(new AWSCredentialsProvider() {
                    @Override
                    public AWSCredentials getCredentials() {
                        return new AWSCredentials() {
                            @Override
                            public String getAWSAccessKeyId() {
                                return accessKeyId;
                            }

                            @Override
                            public String getAWSSecretKey() {
                                return secretAccessKey;
                            }
                        };
                    }

                    @Override
                    public void refresh() {
//                        logger.warn("AWS Key refresh not implemented");
                    }
                })
                .build();

        GetAuthorizationTokenRequest getAuthTokenRequest = new GetAuthorizationTokenRequest();
        List<String> registryIds = new ArrayList<>();
        registryIds.add(registryId);
        getAuthTokenRequest.setRegistryIds(registryIds);

        // Get Authorization Token
        GetAuthorizationTokenResult getAuthTokenResult = ecrClient.getAuthorizationToken(getAuthTokenRequest);
        AuthorizationData authData = getAuthTokenResult.getAuthorizationData().get(0);
        String userPassword = StringUtils.newStringUtf8(Base64.decodeBase64(authData.getAuthorizationToken()));
        String user = userPassword.substring(0, userPassword.indexOf(":"));
        String password = userPassword.substring(userPassword.indexOf(":") + 1);
        String registryUrl = authData.getProxyEndpoint();

        return Triple.of(registryUrl, user, password);
    }

    public static String getTag(String baseUrl, String repo, String imageTag) {
        return appendToTrailIfNotExists(baseUrl, "/")
                + appendToTrailIfNotExists(repo, ":")
                + imageTag;
    }

    private static String appendToTrailIfNotExists(String baseUrl, String trail) {
        if (!baseUrl.endsWith(trail)) {
            return baseUrl + trail;
        }
        return baseUrl;
    }
}
