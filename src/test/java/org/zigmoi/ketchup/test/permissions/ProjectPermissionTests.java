package org.zigmoi.ketchup.test.permissions;

import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import org.zigmoi.ketchup.project.dtos.ProjectAclDto;
import org.zigmoi.ketchup.project.entities.ProjectAcl;
import org.zigmoi.ketchup.project.repositories.ProjectAclRepository;
import org.zigmoi.ketchup.project.services.ProjectAclService;
import org.zigmoi.ketchup.iam.entities.User;
import org.zigmoi.ketchup.iam.repositories.UserRepository;
import org.zigmoi.ketchup.project.entities.ProjectId;

import java.util.stream.Collectors;
import java.util.stream.Stream;

@RunWith(SpringRunner.class)
@SpringBootTest
//@TestPropertySource(locations = "classpath:application-test.properties")
@ActiveProfiles("test")
@Sql(scripts = {
        "classpath:test-scripts/test-data.sql",
        "classpath:test-scripts/test-permission-data.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class ProjectPermissionTests {

    @Autowired
    private ProjectAclService projectAclService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectAclRepository projectAclRepository;

    @BeforeEach
    void initUseCase() {
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

        ProjectId projectId = new ProjectId();
        projectId.setTenantId("t1.com");
        projectId.setResourceId("p1");

        ProjectId projectIdAll = new ProjectId();
        projectIdAll.setTenantId("t1.com");
        projectIdAll.setResourceId("*");


        //   System.out.println(projectAclService.hasProjectPermission("u1@t1.com", "assign-read-project", projectId));
//        System.out.println(projectAclService.hasProjectPermission("admin@t1.com", "assign-read-project", projectId));
//        System.out.println(projectAclService.hasProjectPermission("u1@t1.com", "assign-read-project", projectIdAll));
//        System.out.println(projectAclService.hasProjectPermission("admin@t1.com", "assign-read-project", projectIdAll));
//        //  assert (projectAclService.hasProjectPermission("admin@t1.com", "update-project", projectId));

        ProjectAclDto projectAclDto = new ProjectAclDto();
        projectAclDto.setIdentity("u1@t1.com");
        projectAclDto.setResourceId("p1");
        projectAclDto.setPermissions(Stream.of("read-project").collect(Collectors.toSet()));
        projectAclService.assignPermission(projectAclDto);
        assert (projectAclService.hasProjectPermission("u1@t1.com", "read-project", projectId));
    }

    @Test
    @WithMockUser(username = "admin@t1.com")
    public void verifyAssignPermissionOnAllProjects() {
        ProjectId projectId = new ProjectId();
        projectId.setTenantId("t1.com");
        projectId.setResourceId("p1");

        ProjectId projectIdAll = new ProjectId();
        projectIdAll.setTenantId("t1.com");
        projectIdAll.setResourceId("*");

        ProjectAclDto projectAclDto = new ProjectAclDto();
        projectAclDto.setIdentity("u1@t1.com");
        projectAclDto.setResourceId("*");
        projectAclDto.setPermissions(Stream.of("read-project").collect(Collectors.toSet()));
        projectAclService.assignPermission(projectAclDto);
        assert (projectAclService.hasProjectPermission("u1@t1.com", "read-project", projectIdAll));
        assert (projectAclService.hasProjectPermission("u1@t1.com", "read-project", projectId));
    }

    @Test
    @WithMockUser(username = "admin@t1.com")
    public void verifyRevokePermissionOnSpecificProject() {
        ProjectId projectId = new ProjectId();
        projectId.setTenantId("t1.com");
        projectId.setResourceId("p1");

        ProjectId projectIdAll = new ProjectId();
        projectIdAll.setTenantId("t1.com");
        projectIdAll.setResourceId("*");

        ProjectAclDto projectAclDto = new ProjectAclDto();
        projectAclDto.setIdentity("u1@t1.com");
        projectAclDto.setResourceId("p1");
        projectAclDto.setPermissions(Stream.of("read-project").collect(Collectors.toSet()));
        projectAclService.revokePermission(projectAclDto);
        assert (projectAclService.hasProjectPermission("u1@t1.com", "read-project", projectId) == false);
    }

    @Test
    @WithMockUser(username = "admin@t1.com")
    public void verifyRevokePermissionOnAllProjects() {
        ProjectId projectId = new ProjectId();
        projectId.setTenantId("t1.com");
        projectId.setResourceId("p1");

        ProjectId projectIdAll = new ProjectId();
        projectIdAll.setTenantId("t1.com");
        projectIdAll.setResourceId("*");

        ProjectAclDto projectAclDto = new ProjectAclDto();
        projectAclDto.setIdentity("u1@t1.com");
        projectAclDto.setResourceId("*");
        projectAclDto.setPermissions(Stream.of("read-project").collect(Collectors.toSet()));
        projectAclService.revokePermission(projectAclDto);
        assert (projectAclService.hasProjectPermission("u1@t1.com", "read-project", projectIdAll) == false);
        assert (projectAclService.hasProjectPermission("u1@t1.com", "read-project", projectId) == false);
    }


    @Test
    @WithMockUser(username = "admin@t1.com")
    public void verifyPermissions_When_AssignSpecific_RevokeSpecific() {
        ProjectId projectId = new ProjectId();
        projectId.setTenantId("t1.com");
        projectId.setResourceId("p1");

        ProjectId projectIdAll = new ProjectId();
        projectIdAll.setTenantId("t1.com");
        projectIdAll.setResourceId("*");


        ProjectAclDto projectAclDto = new ProjectAclDto();
        projectAclDto.setIdentity("u1@t1.com");
        projectAclDto.setResourceId("p1");
        projectAclDto.setPermissions(Stream.of("read-project").collect(Collectors.toSet()));
        projectAclService.assignPermission(projectAclDto);
        assert (projectAclService.hasProjectPermission("u1@t1.com", "read-project", projectId));
        projectAclService.revokePermission(projectAclDto);
        assert (projectAclService.hasProjectPermission("u1@t1.com", "read-project", projectId) == false);
    }

    @Test
    @WithMockUser(username = "admin@t1.com")
    public void verifyPermissions_When_AssignSpecific_RevokeAll() {
        ProjectId projectId = new ProjectId();
        projectId.setTenantId("t1.com");
        projectId.setResourceId("p1");

        ProjectId projectIdAll = new ProjectId();
        projectIdAll.setTenantId("t1.com");
        projectIdAll.setResourceId("*");

        ProjectAclDto projectAclDto = new ProjectAclDto();
        projectAclDto.setIdentity("u1@t1.com");
        projectAclDto.setResourceId("p1");
        projectAclDto.setPermissions(Stream.of("read-project").collect(Collectors.toSet()));
        projectAclService.assignPermission(projectAclDto);
        assert (projectAclService.hasProjectPermission("u1@t1.com", "read-project", projectIdAll) == false);
        assert (projectAclService.hasProjectPermission("u1@t1.com", "read-project", projectId));

        projectAclDto.setResourceId("*");
        projectAclService.revokePermission(projectAclDto);
        assert (projectAclService.hasProjectPermission("u1@t1.com", "read-project", projectIdAll) == false);
        assert (projectAclService.hasProjectPermission("u1@t1.com", "read-project", projectId) == false);
    }

    @Test
    @WithMockUser(username = "admin@t1.com")
    public void verifyPermissions_When_AssignAll_RevokeSpecific() {
        ProjectId projectId = new ProjectId();
        projectId.setTenantId("t1.com");
        projectId.setResourceId("p1");

        ProjectId projectIdAll = new ProjectId();
        projectIdAll.setTenantId("t1.com");
        projectIdAll.setResourceId("*");

        ProjectAclDto projectAclDto = new ProjectAclDto();
        projectAclDto.setIdentity("u1@t1.com");
        projectAclDto.setResourceId("*");
        projectAclDto.setPermissions(Stream.of("read-project").collect(Collectors.toSet()));
        projectAclService.assignPermission(projectAclDto);
        assert (projectAclService.hasProjectPermission("u1@t1.com", "read-project", projectIdAll));
        assert (projectAclService.hasProjectPermission("u1@t1.com", "read-project", projectId));

        projectAclDto.setResourceId("p1");
        projectAclService.revokePermission(projectAclDto);
        assert (projectAclService.hasProjectPermission("u1@t1.com", "read-project", projectIdAll));
        assert (projectAclService.hasProjectPermission("u1@t1.com", "read-project", projectId) == false);
    }

    @Test
    @WithMockUser(username = "admin@t1.com")
    public void verifyPermissions_When_AssignAll_RevokeAll() {
        ProjectId projectId = new ProjectId();
        projectId.setTenantId("t1.com");
        projectId.setResourceId("p1");

        ProjectId projectIdAll = new ProjectId();
        projectIdAll.setTenantId("t1.com");
        projectIdAll.setResourceId("*");

        ProjectAclDto projectAclDto = new ProjectAclDto();
        projectAclDto.setIdentity("u1@t1.com");
        projectAclDto.setResourceId("*");
        projectAclDto.setPermissions(Stream.of("read-project").collect(Collectors.toSet()));
        projectAclService.assignPermission(projectAclDto);
        assert (projectAclService.hasProjectPermission("u1@t1.com", "read-project", projectIdAll));
        assert (projectAclService.hasProjectPermission("u1@t1.com", "read-project", projectId));

        projectAclService.revokePermission(projectAclDto);
        assert (projectAclService.hasProjectPermission("u1@t1.com", "read-project", projectIdAll) == false);
        assert (projectAclService.hasProjectPermission("u1@t1.com", "read-project", projectId) == false);
    }


    @Test
    @WithMockUser(username = "admin@t1.com")
    public void verifyPermissions_When_RevokeSpecific_AssignSpecific() {
        ProjectId projectId = new ProjectId();
        projectId.setTenantId("t1.com");
        projectId.setResourceId("p1");

        ProjectId projectIdAll = new ProjectId();
        projectIdAll.setTenantId("t1.com");
        projectIdAll.setResourceId("*");


        ProjectAclDto projectAclDto = new ProjectAclDto();
        projectAclDto.setIdentity("u1@t1.com");
        projectAclDto.setResourceId("p1");
        projectAclDto.setPermissions(Stream.of("read-project").collect(Collectors.toSet()));
        projectAclService.revokePermission(projectAclDto);
        assert (projectAclService.hasProjectPermission("u1@t1.com", "read-project", projectId) == false);
        projectAclService.assignPermission(projectAclDto);
        assert (projectAclService.hasProjectPermission("u1@t1.com", "read-project", projectId));

    }

    @Test
    @WithMockUser(username = "admin@t1.com")
    public void verifyPermissions_When_RevokeAll_AssignSpecific() {
        ProjectId projectId = new ProjectId();
        projectId.setTenantId("t1.com");
        projectId.setResourceId("p1");

        ProjectId projectIdAll = new ProjectId();
        projectIdAll.setTenantId("t1.com");
        projectIdAll.setResourceId("*");

        ProjectAclDto projectAclDto = new ProjectAclDto();
        projectAclDto.setIdentity("u1@t1.com");
        projectAclDto.setResourceId("*");
        projectAclDto.setPermissions(Stream.of("read-project").collect(Collectors.toSet()));
        projectAclService.revokePermission(projectAclDto);
        assert (projectAclService.hasProjectPermission("u1@t1.com", "read-project", projectIdAll) == false);
        assert (projectAclService.hasProjectPermission("u1@t1.com", "read-project", projectId) == false);

        projectAclDto.setResourceId("p1");
        projectAclService.assignPermission(projectAclDto);
        assert (projectAclService.hasProjectPermission("u1@t1.com", "read-project", projectIdAll) == false);
        assert (projectAclService.hasProjectPermission("u1@t1.com", "read-project", projectId));
    }

    @Test
    @WithMockUser(username = "admin@t1.com")
    public void verifyPermissions_When_RevokeAll_AssignAll() {
        ProjectId projectId = new ProjectId();
        projectId.setTenantId("t1.com");
        projectId.setResourceId("p1");

        ProjectId projectIdAll = new ProjectId();
        projectIdAll.setTenantId("t1.com");
        projectIdAll.setResourceId("*");

        ProjectAclDto projectAclDto = new ProjectAclDto();
        projectAclDto.setIdentity("u1@t1.com");
        projectAclDto.setResourceId("*");
        projectAclDto.setPermissions(Stream.of("read-project").collect(Collectors.toSet()));
        projectAclService.revokePermission(projectAclDto);
        assert (projectAclService.hasProjectPermission("u1@t1.com", "read-project", projectIdAll) == false);
        assert (projectAclService.hasProjectPermission("u1@t1.com", "read-project", projectId) == false);

        projectAclService.assignPermission(projectAclDto);
        assert (projectAclService.hasProjectPermission("u1@t1.com", "read-project", projectIdAll));
        assert (projectAclService.hasProjectPermission("u1@t1.com", "read-project", projectId));
    }

    @Test
    @WithMockUser(username = "admin@t1.com")
    public void verifyPermissions_When_RevokeSpecific_AssignAll() {
        ProjectId projectId = new ProjectId();
        projectId.setTenantId("t1.com");
        projectId.setResourceId("p1");

        ProjectId projectIdAll = new ProjectId();
        projectIdAll.setTenantId("t1.com");
        projectIdAll.setResourceId("*");

        ProjectAclDto projectAclDto = new ProjectAclDto();
        projectAclDto.setIdentity("u1@t1.com");
        projectAclDto.setResourceId("p1");
        projectAclDto.setPermissions(Stream.of("read-project").collect(Collectors.toSet()));
        projectAclService.revokePermission(projectAclDto);
        assert (projectAclService.hasProjectPermission("u1@t1.com", "read-project", projectIdAll) == false);
        assert (projectAclService.hasProjectPermission("u1@t1.com", "read-project", projectId) == false);

        projectAclDto.setResourceId("*");
        projectAclService.assignPermission(projectAclDto);
        assert (projectAclService.hasProjectPermission("u1@t1.com", "read-project", projectIdAll));
        assert (projectAclService.hasProjectPermission("u1@t1.com", "read-project", projectId));
    }


}
