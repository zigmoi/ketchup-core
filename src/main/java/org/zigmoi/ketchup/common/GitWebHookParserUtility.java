package org.zigmoi.ketchup.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.json.JSONObject;

public class GitWebHookParserUtility {

    private static final String VENDOR_GITLAB = "gitlab";
    private static final String VENDOR_GITHUB = "github";

    public static WebhookEvent parseEvent(String vendor, String deploymentId, String msg) {
        WebhookEvent webhookEvent;
        switch (vendor) {
            case VENDOR_GITLAB:
                webhookEvent = parseGitlabWebhookEvent(msg);
                webhookEvent.setVendor(vendor);
                webhookEvent.setDeploymentId(deploymentId);
                return webhookEvent;
            case VENDOR_GITHUB:
                webhookEvent = parseGithubWebhookEvent(msg);
                webhookEvent.setVendor(vendor);
                webhookEvent.setDeploymentId(deploymentId);
                return webhookEvent;
            default:
                throw new IllegalStateException("Unexpected value: " + vendor);
        }
    }

    private static WebhookEvent parseGitlabWebhookEvent(String msg) {
        System.out.println("webhook event gitlab: " + msg);
        JSONObject jo = new JSONObject(msg);
        return new WebhookEvent(jo.getString("event_name"), getGitlabBranchName(jo), getCommitId(jo));
    }

    private static WebhookEvent parseGithubWebhookEvent(String msg) {
        System.out.println("webhook event gitlab: " + msg);
        JSONObject jo = new JSONObject(msg);
        //TODO github payload does not have detials about event
        // type, although we can configure what events to send in github
        // and we can consider this as push event only for now.
        return new WebhookEvent("push", getGitlabBranchName(jo), getCommitId(jo));
    }

    //same for gitlab and github
    private static String getCommitId(JSONObject jsonObject){
        return jsonObject.getString("after");
    }

    //same for gitlab and github
    private static String getGitlabBranchName(JSONObject jo) {
        String[] refs = jo.getString("ref").split("/");
        return refs[refs.length - 1];
    }

    public static boolean isPushEvent(WebhookEvent event) {
        return "push".equalsIgnoreCase(event.getEventName());
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class WebhookEvent {
        private String vendor;
        private String deploymentId;
        private String eventName; // push, tag, etc
        private String branchName;
        private String commitId;

        public WebhookEvent(String eventName, String branchName, String commitId) {
            this.eventName = eventName;
            this.branchName = branchName;
            this.commitId = commitId;
        }
    }
}
