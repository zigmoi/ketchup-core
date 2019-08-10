package org.zigmoi.ketchup.iam.controllers;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.logging.Logger;

@RestController
public class TenantResourceController {
    private static final Logger logger = Logger.getLogger(TenantResourceController.class.toString());

    @PostMapping("/v1/resource/createBuildTool")
    public void createBuildTool(HttpServletRequest servletRequest,
                                @RequestParam("file[]") MultipartFile[] files,
                                @RequestParam String settingName) {
        logger.info("Upload File Count - " + files.length);
        logger.info("settingName - " + settingName);
    }
}
