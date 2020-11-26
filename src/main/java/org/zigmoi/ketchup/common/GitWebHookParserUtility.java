package org.zigmoi.ketchup.common;

import com.google.gson.JsonObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.json.JSONObject;

public class GitWebHookParserUtility {

    private static final String VENDOR_GITLAB = "gitlab";

    public static WebhookEvent parseEvent(String vendor, String deploymentId, String msg) {
        switch (vendor) {
            case VENDOR_GITLAB:
                WebhookEvent webhookEvent = parseGitlabWebhookEvent(msg);
                webhookEvent.setVendor(vendor);
                webhookEvent.setDeploymentId(deploymentId);
                return webhookEvent;
            default:
                throw new IllegalStateException("Unexpected value: " + vendor);
        }
    }

    private static WebhookEvent parseGitlabWebhookEvent(String msg) {
        System.out.println("webhook event: " + msg);
        JSONObject jo = new JSONObject(msg);
        return new WebhookEvent(jo.getString("event_name"), getGitlabBranchName(jo), getCommitId(jo));
    }

    private static String getCommitId(JSONObject jsonObject){
        return jsonObject.getString("after");
    }

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
        private String vendor; // push, tag, etc
        private String deploymentId; // push, tag, etc
        private String eventName; // push, tag, etc
        private String branchName;
        private String commitId;

        public WebhookEvent(String eventName, String branchName) {
            this.eventName = eventName;
            this.branchName = branchName;
        }

        public WebhookEvent(String eventName, String branchName, String commitId) {
            this.eventName = eventName;
            this.branchName = branchName;
            this.commitId = commitId;
        }
    }
}
