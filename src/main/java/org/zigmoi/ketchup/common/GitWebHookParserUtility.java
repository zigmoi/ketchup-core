package org.zigmoi.ketchup.common;

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
        JSONObject jo = new JSONObject(msg);
        return new WebhookEvent(jo.getString("event_name"), getGitlabBranchName(jo));
    }

    private static String getGitlabBranchName(JSONObject jo) {
        String[] refs = jo.getString("ref").split("/");
        return refs[refs.length-1];
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

        public WebhookEvent(String eventName, String branchName) {
            this.eventName = eventName;
            this.branchName = branchName;
        }
    }
}
