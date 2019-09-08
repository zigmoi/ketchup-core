package org.zigmoi.ketchup.test.permissions;

import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import org.zigmoi.ketchup.iam.authz.repositories.ProjectAclRepository;
import org.zigmoi.ketchup.iam.authz.services.ProjectAclService;
import org.zigmoi.ketchup.iam.authz.services.ProjectAclServiceImpl;
import org.zigmoi.ketchup.project.entities.ProjectId;
import org.zigmoi.ketchup.project.repositories.ProjectRepository;

@RunWith(SpringRunner.class)
@DataJpaTest
@TestPropertySource(locations = "classpath:application-tests.properties")
public class ProjectPermissionTests {

//    @Autowired
//    private TestEntityManager entityManager;

    @Autowired
    private ProjectAclRepository projectAclRepository;

    @Autowired
    private ProjectRepository projectRepository;

//    @Autowired
//    private UserRepository userRepository;


//    @Autowired
//    private BCryptPasswordEncoder passwordEncoder;
    @Autowired
    private ProjectAclService projectAclService;
 //   private ProjectService projectService;
    //private UserService userService;

    @BeforeEach
    void initUseCase() {
       // userService = new UserServiceImpl();
//        projectService = new ProjectServiceImpl(projectRepository, userService, projectAclService);
       // projectAclService = new ProjectAclServiceImpl(projectAclRepository, projectRepository);
    }


    @Test
    @WithMockUser(username="admin@t1.com")
    @Sql(scripts = {
            "classpath:test-scripts/test-data.sql",
            "classpath:test-scripts/test-permission-data.sql"})
    public void whenFindByName_thenReturnEmployee() {
//        // given
//
//        entityManager.persist(alex);
//        entityManager.flush();
        ProjectId projectId = new ProjectId();
        projectId.setTenantId("t1.com");
        projectId.setResourceId("p1");
        assert (projectAclService.hasProjectPermission("admin@t1.com", "read-project", projectId));

    }

}
