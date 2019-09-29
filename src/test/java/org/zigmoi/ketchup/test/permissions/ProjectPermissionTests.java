package org.zigmoi.ketchup.test.permissions;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Assert;
import org.zigmoi.ketchup.iam.commons.AuthUtils;
import org.zigmoi.ketchup.iam.entities.User;
import org.zigmoi.ketchup.iam.repositories.UserRepository;
import org.zigmoi.ketchup.project.dtos.ProjectAclDto;
import org.zigmoi.ketchup.project.entities.ProjectAcl;
import org.zigmoi.ketchup.project.repositories.ProjectAclRepository;
import org.zigmoi.ketchup.project.services.ProjectAclService;

import java.util.stream.Collectors;
import java.util.stream.Stream;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
@SqlGroup({
        @Sql(scripts = {
                "classpath:test-scripts/test-data.sql",
                "classpath:test-scripts/test-permission-data.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
        @Sql(scripts = {"classpath:test-scripts/clear-data.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
})
public class ProjectPermissionTests {

    @Autowired
    private ProjectAclService projectAclService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectAclRepository projectAclRepository;


    @Test
    @WithMockUser(username = "admin@t2.com")
    public void verifyOtherTenantsPermissions() {
        System.out.println(AuthUtils.getCurrentTenantId());
        String projectId = "p1";
        String projectIdAll = "*";
        Assert.isTrue(projectAclService.hasProjectPermission("admin@t1.com", "read-project", projectId) == false,
                "User u1@t2.com has extra read-project permission on project p1.");
        Assert.isTrue(projectAclService.hasProjectPermission("admin@t1.com", "read-project", projectIdAll) == false,
                "User u1@t2.com has extra read-project permission on all projects.");
    }

    @Test
    @WithMockUser(username = "admin@t1.com")
    public void verifyAssignPermissionOnSpecificProject() {

        for (User u : userRepository.findAll()) {
            System.out.println(u.getUsername());
        }

        for (ProjectAcl acl : projectAclRepository.findAll()) {
            System.out.println(acl.toString());
        }

        String projectId = "p1";
        String projectIdAll = "*";

        ProjectAclDto projectAclDto = new ProjectAclDto();
        projectAclDto.setIdentity("u1@t1.com");
        projectAclDto.setProjectResourceId("p1");
        projectAclDto.setPermissions(Stream.of("read-project").collect(Collectors.toSet()));
        projectAclService.assignPermission(projectAclDto);
        Assert.isTrue(projectAclService.hasProjectPermission("u1@t1.com", "read-project", projectId),
                "User does not have assigned read-project permission.");
        //check other permissions and users are unaffected.
        Assert.isTrue(projectAclService.hasProjectPermission("u1@t1.com", "update-project", projectId) == false,
                "User u1@t1.com has extra update-project permission on project p1.");
        Assert.isTrue(projectAclService.hasProjectPermission("u2@t1.com", "update-project", projectId) == false,
                "User u2@t1.com has extra update-project permission on project p1.");
        Assert.isTrue(projectAclService.hasProjectPermission("u2@t1.com", "read-project", projectId) == false,
                "User u2@t1.com has extra read-project permission on project p1.");

        projectId = "p2";
        Assert.isTrue(projectAclService.hasProjectPermission("u1@t1.com", "read-project", projectId) == false,
                "User has extra read-project permission on project p2.");
        Assert.isTrue(projectAclService.hasProjectPermission("u1@t1.com", "update-project", projectId) == false,
                "User has extra update-project permission on project p2.");
        Assert.isTrue(projectAclService.hasProjectPermission("u2@t1.com", "update-project", projectId) == false,
                "User u2@t1.com has extra update-project permission on project p2.");
        Assert.isTrue(projectAclService.hasProjectPermission("u2@t1.com", "read-project", projectId) == false,
                "User u2@t1.com has extra read-project permission on project p2.");
    }

    @Test
    @WithMockUser(username = "admin@t1.com")
    public void verifyAssignPermissionOnAllProjects() {
        String projectId = "p1";
        String projectIdAll = "*";

        ProjectAclDto projectAclDto = new ProjectAclDto();
        projectAclDto.setIdentity("u1@t1.com");
        projectAclDto.setProjectResourceId("*");
        projectAclDto.setPermissions(Stream.of("read-project").collect(Collectors.toSet()));
        projectAclService.assignPermission(projectAclDto);
        Assert.isTrue(projectAclService.hasProjectPermission("u1@t1.com", "read-project", projectIdAll),
                "User u1@t1.com does not have read-project permission on all projects.");
        Assert.isTrue(projectAclService.hasProjectPermission("u1@t1.com", "read-project", projectId),
                "User u1@t1.com does not have read-project permission on project p1..");

        //Check for extra permissions
        Assert.isTrue(projectAclService.hasProjectPermission("u1@t1.com", "update-project", projectId) == false,
                "User u1@t1.com has extra update-project permission on project p1.");
        Assert.isTrue(projectAclService.hasProjectPermission("u2@t1.com", "update-project", projectId) == false,
                "User u2@t1.com has extra update-project permission on project p1.");
        Assert.isTrue(projectAclService.hasProjectPermission("u2@t1.com", "read-project", projectId) == false,
                "User u2@t1.com has extra read-project permission on project p1.");

        projectId = "p2";
        Assert.isTrue(projectAclService.hasProjectPermission("u1@t1.com", "read-project", projectId),
                "User u1@t1.com should have read-project permission on project p2.");
        Assert.isTrue(projectAclService.hasProjectPermission("u1@t1.com", "update-project", projectId) == false,
                "User u1@t1.com has extra update-project permission on project p2.");
        Assert.isTrue(projectAclService.hasProjectPermission("u2@t1.com", "update-project", projectId) == false,
                "User u2@t1.com has extra update-project permission on project p2.");
        Assert.isTrue(projectAclService.hasProjectPermission("u2@t1.com", "read-project", projectId) == false,
                "User u2@t1.com has extra read-project permission on project p2.");
    }

    @Test
    @WithMockUser(username = "admin@t1.com")
    public void verifyRevokePermissionOnSpecificProject() {
        String projectId = "p1";
        String projectIdAll = "*";

        ProjectAclDto projectAclDto = new ProjectAclDto();
        projectAclDto.setIdentity("u1@t1.com");
        projectAclDto.setProjectResourceId("p1");
        projectAclDto.setPermissions(Stream.of("read-project").collect(Collectors.toSet()));
        projectAclService.revokePermission(projectAclDto);
        Assert.isTrue(projectAclService.hasProjectPermission("u1@t1.com", "read-project", projectId) == false,
                "User u1@t1.com has extra read-project permission on project p1.");
        Assert.isTrue(projectAclService.hasProjectPermission("u1@t1.com", "update-project", projectId) == false,
                "User u1@t1.com has extra update-project permission on project p1.");
        Assert.isTrue(projectAclService.hasProjectPermission("u2@t1.com", "update-project", projectId) == false,
                "User u2@t1.com has extra update-project permission on project p1.");
        Assert.isTrue(projectAclService.hasProjectPermission("u2@t1.com", "read-project", projectId) == false,
                "User u2@t1.com has extra read-project permission on project p1.");

        projectId = "p2";
        Assert.isTrue(projectAclService.hasProjectPermission("u1@t1.com", "read-project", projectId) == false,
                "User u1@t1.com has extra read-project permission on project p2.");
        Assert.isTrue(projectAclService.hasProjectPermission("u1@t1.com", "update-project", projectId) == false,
                "User u1@t1.com has extra update-project permission on project p2.");
        Assert.isTrue(projectAclService.hasProjectPermission("u2@t1.com", "update-project", projectId) == false,
                "User u2@t1.com has extra update-project permission on project p2.");
        Assert.isTrue(projectAclService.hasProjectPermission("u2@t1.com", "read-project", projectId) == false,
                "User u2@t1.com has extra read-project permission on project p2.");
    }

    @Test
    @WithMockUser(username = "admin@t1.com")
    public void verifyRevokePermissionOnAllProjects() {
        String projectId = "p1";
        String projectIdAll = "*";

        ProjectAclDto projectAclDto = new ProjectAclDto();
        projectAclDto.setIdentity("u1@t1.com");
        projectAclDto.setProjectResourceId("*");
        projectAclDto.setPermissions(Stream.of("read-project").collect(Collectors.toSet()));
        projectAclService.revokePermission(projectAclDto);
        Assert.isTrue(projectAclService.hasProjectPermission("u1@t1.com", "read-project", projectIdAll) == false,
                "User u1@t1.com has extra read-project permission on all projects.");
        Assert.isTrue(projectAclService.hasProjectPermission("u1@t1.com", "read-project", projectId) == false,
                "User u1@t1.com has extra read-project permission on project p1.");

        //Check other permissions, other project permissions, other users permissions and other tenant permissions are unchanged.
        Assert.isTrue(projectAclService.hasProjectPermission("u1@t1.com", "update-project", projectId) == false,
                "User u1@t1.com has extra update-project permission on project p1.");
        Assert.isTrue(projectAclService.hasProjectPermission("u2@t1.com", "update-project", projectId) == false,
                "User u2@t1.com has extra update-project permission on project p1.");
        Assert.isTrue(projectAclService.hasProjectPermission("u2@t1.com", "read-project", projectId) == false,
                "User u2@t1.com has extra read-project permission on project p1.");

        projectId = "p2";
        Assert.isTrue(projectAclService.hasProjectPermission("u1@t1.com", "read-project", projectId) == false,
                "User u1@t1.com has extra read-project permission on project p2.");
        Assert.isTrue(projectAclService.hasProjectPermission("u1@t1.com", "update-project", projectId) == false,
                "User u1@t1.com has extra update-project permission on project p2.");
        Assert.isTrue(projectAclService.hasProjectPermission("u2@t1.com", "update-project", projectId) == false,
                "User u2@t1.com has extra update-project permission on project p2.");
        Assert.isTrue(projectAclService.hasProjectPermission("u2@t1.com", "read-project", projectId) == false,
                "User u2@t1.com has extra read-project permission on project p2.");
    }


    @Test
    @WithMockUser(username = "admin@t1.com")
    public void verifyPermissions_When_AssignSpecific_RevokeSpecific() {
        String projectId = "p1";
        String projectIdAll = "*";

        ProjectAclDto projectAclDto = new ProjectAclDto();
        projectAclDto.setIdentity("u1@t1.com");
        projectAclDto.setProjectResourceId("p1");
        projectAclDto.setPermissions(Stream.of("read-project").collect(Collectors.toSet()));
        projectAclService.assignPermission(projectAclDto);
        Assert.isTrue(projectAclService.hasProjectPermission("u1@t1.com", "read-project", projectId),
                "User u1@t1.com does not have read-project permission on project p1.");
        projectAclService.revokePermission(projectAclDto);
        Assert.isTrue(projectAclService.hasProjectPermission("u1@t1.com", "read-project", projectId) == false,
                "User u1@t1.com has extra read-project permission on project p1.");
    }

    @Test
    @WithMockUser(username = "admin@t1.com")
    public void verifyPermissions_When_AssignSpecific_RevokeAll() {
        String projectId = "p1";
        String projectIdAll = "*";

        ProjectAclDto projectAclDto = new ProjectAclDto();
        projectAclDto.setIdentity("u1@t1.com");
        projectAclDto.setProjectResourceId("p1");
        projectAclDto.setPermissions(Stream.of("read-project").collect(Collectors.toSet()));
        projectAclService.assignPermission(projectAclDto);
        Assert.isTrue(projectAclService.hasProjectPermission("u1@t1.com", "read-project", projectIdAll) == false,
                "User u1@t1.com has extra read-project permission on all projects.");
        Assert.isTrue(projectAclService.hasProjectPermission("u1@t1.com", "read-project", projectId),
                "User u1@t1.com does not have read-project permission on project p1.");

        projectAclDto.setProjectResourceId("*");
        projectAclService.revokePermission(projectAclDto);
        Assert.isTrue(projectAclService.hasProjectPermission("u1@t1.com", "read-project", projectIdAll) == false,
                "User u1@t1.com has extra read-project permission on all projects.");
        Assert.isTrue(projectAclService.hasProjectPermission("u1@t1.com", "read-project", projectId) == false,
                "User u1@t1.com has extra read-project permission on project p1.");
    }

    @Test
    @WithMockUser(username = "admin@t1.com")
    public void verifyPermissions_When_AssignAll_RevokeSpecific() {
        String projectId = "p1";
        String projectIdAll = "*";

        ProjectAclDto projectAclDto = new ProjectAclDto();
        projectAclDto.setIdentity("u1@t1.com");
        projectAclDto.setProjectResourceId("*");
        projectAclDto.setPermissions(Stream.of("read-project").collect(Collectors.toSet()));
        projectAclService.assignPermission(projectAclDto);
        Assert.isTrue(projectAclService.hasProjectPermission("u1@t1.com", "read-project", projectIdAll));
        Assert.isTrue(projectAclService.hasProjectPermission("u1@t1.com", "read-project", projectId));

        projectAclDto.setProjectResourceId("p1");
        projectAclService.revokePermission(projectAclDto);
        Assert.isTrue(projectAclService.hasProjectPermission("u1@t1.com", "read-project", projectIdAll));
        Assert.isTrue(projectAclService.hasProjectPermission("u1@t1.com", "read-project", projectId) == false);
    }

    @Test
    @WithMockUser(username = "admin@t1.com")
    public void verifyPermissions_When_AssignAll_RevokeAll() {
        String projectId = "p1";
        String projectIdAll = "*";

        ProjectAclDto projectAclDto = new ProjectAclDto();
        projectAclDto.setIdentity("u1@t1.com");
        projectAclDto.setProjectResourceId("*");
        projectAclDto.setPermissions(Stream.of("read-project").collect(Collectors.toSet()));
        projectAclService.assignPermission(projectAclDto);
        Assert.isTrue(projectAclService.hasProjectPermission("u1@t1.com", "read-project", projectIdAll));
        Assert.isTrue(projectAclService.hasProjectPermission("u1@t1.com", "read-project", projectId));

        projectAclService.revokePermission(projectAclDto);
        Assert.isTrue(projectAclService.hasProjectPermission("u1@t1.com", "read-project", projectIdAll) == false);
        Assert.isTrue(projectAclService.hasProjectPermission("u1@t1.com", "read-project", projectId) == false);
    }


    @Test
    @WithMockUser(username = "admin@t1.com")
    public void verifyPermissions_When_RevokeSpecific_AssignSpecific() {
        String projectId = "p1";
        String projectIdAll = "*";

        ProjectAclDto projectAclDto = new ProjectAclDto();
        projectAclDto.setIdentity("u1@t1.com");
        projectAclDto.setProjectResourceId("p1");
        projectAclDto.setPermissions(Stream.of("read-project").collect(Collectors.toSet()));
        projectAclService.revokePermission(projectAclDto);
        Assert.isTrue(projectAclService.hasProjectPermission("u1@t1.com", "read-project", projectId) == false);
        projectAclService.assignPermission(projectAclDto);
        Assert.isTrue(projectAclService.hasProjectPermission("u1@t1.com", "read-project", projectId));

    }

    @Test
    @WithMockUser(username = "admin@t1.com")
    public void verifyPermissions_When_RevokeAll_AssignSpecific() {
        String projectId = "p1";
        String projectIdAll = "*";

        ProjectAclDto projectAclDto = new ProjectAclDto();
        projectAclDto.setIdentity("u1@t1.com");
        projectAclDto.setProjectResourceId("*");
        projectAclDto.setPermissions(Stream.of("read-project").collect(Collectors.toSet()));
        projectAclService.revokePermission(projectAclDto);
        Assert.isTrue(projectAclService.hasProjectPermission("u1@t1.com", "read-project", projectIdAll) == false);
        Assert.isTrue(projectAclService.hasProjectPermission("u1@t1.com", "read-project", projectId) == false);

        projectAclDto.setProjectResourceId("p1");
        projectAclService.assignPermission(projectAclDto);
        Assert.isTrue(projectAclService.hasProjectPermission("u1@t1.com", "read-project", projectIdAll) == false);
        Assert.isTrue(projectAclService.hasProjectPermission("u1@t1.com", "read-project", projectId));
    }

    @Test
    @WithMockUser(username = "admin@t1.com")
    public void verifyPermissions_When_RevokeAll_AssignAll() {
        String projectId = "p1";
        String projectIdAll = "*";

        ProjectAclDto projectAclDto = new ProjectAclDto();
        projectAclDto.setIdentity("u1@t1.com");
        projectAclDto.setProjectResourceId("*");
        projectAclDto.setPermissions(Stream.of("read-project").collect(Collectors.toSet()));
        projectAclService.revokePermission(projectAclDto);
        Assert.isTrue(projectAclService.hasProjectPermission("u1@t1.com", "read-project", projectIdAll) == false);
        Assert.isTrue(projectAclService.hasProjectPermission("u1@t1.com", "read-project", projectId) == false);

        projectAclService.assignPermission(projectAclDto);
        Assert.isTrue(projectAclService.hasProjectPermission("u1@t1.com", "read-project", projectIdAll));
        Assert.isTrue(projectAclService.hasProjectPermission("u1@t1.com", "read-project", projectId));
    }

    @Test
    @WithMockUser(username = "admin@t1.com")
    public void verifyPermissions_When_RevokeSpecific_AssignAll() {
        String projectId = "p1";
        String projectIdAll = "*";

        ProjectAclDto projectAclDto = new ProjectAclDto();
        projectAclDto.setIdentity("u1@t1.com");
        projectAclDto.setProjectResourceId("p1");
        projectAclDto.setPermissions(Stream.of("read-project").collect(Collectors.toSet()));
        projectAclService.revokePermission(projectAclDto);
        Assert.isTrue(projectAclService.hasProjectPermission("u1@t1.com", "read-project", projectIdAll) == false);
        Assert.isTrue(projectAclService.hasProjectPermission("u1@t1.com", "read-project", projectId) == false);

        projectAclDto.setProjectResourceId("*");
        projectAclService.assignPermission(projectAclDto);
        Assert.isTrue(projectAclService.hasProjectPermission("u1@t1.com", "read-project", projectIdAll));
        Assert.isTrue(projectAclService.hasProjectPermission("u1@t1.com", "read-project", projectId));
    }


}
