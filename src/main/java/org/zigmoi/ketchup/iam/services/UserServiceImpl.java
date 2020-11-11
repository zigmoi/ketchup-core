package org.zigmoi.ketchup.iam.services;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.server.ResponseStatusException;
import org.zigmoi.ketchup.iam.commons.AuthUtils;
import org.zigmoi.ketchup.iam.entities.Tenant;
import org.zigmoi.ketchup.iam.entities.User;
import org.zigmoi.ketchup.iam.exceptions.TenantInActiveException;
import org.zigmoi.ketchup.iam.exceptions.TenantNotFoundException;
import org.zigmoi.ketchup.iam.repositories.UserRepository;
import org.zigmoi.ketchup.application.repositories.RevisionRepository;

import javax.validation.Valid;
import java.util.*;

@Service("userDetailsService")
public class UserServiceImpl implements UserDetailsService, UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TenantService tenantService;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

//    @Autowired
//    private RevisionRepository revisionRepository;


    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {
        Assert.hasLength(userName, "UserName cannot be empty.");
        //Get user from db.
        User user = userRepository.findById(userName)
                .orElseThrow(() -> new UsernameNotFoundException("User account can not be located!"));

        //Check tenant active for this user.
        Tenant tenant = tenantService.getTenant(user.getTenantId()).orElseThrow(() -> new TenantNotFoundException(String.format("Tenant not found for user %s", user.getUsername())));
        if (!tenant.isEnabled()) {
            throw new TenantInActiveException(String.format("Tenant not active for user %s", user.getUsername()));
        }

        return user;
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('ROLE_TENANT_ADMIN', 'ROLE_USER_ADMIN')")
    public void createUser(User user) {
        validateTenantId(user.getUsername());

        if (userRepository.findById(user.getUsername()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format("User with user name %s already exists!", user.getUsername()));
        }

        if (!AuthUtils.matchesPolicy(user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format("User Password %s is invalid!", user.getPassword()));
        }


        if (Arrays.asList("ROLE_TENANT_ADMIN", "ROLE_USER_ADMIN", "ROLE_USER_READER", "ROLE_USER")
                .containsAll(user.getRoles()) == false) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid role found!");
        }

        String loggedInUserName = AuthUtils.getCurrentQualifiedUsername();
        User loggedInUser = userRepository.findById(loggedInUserName).orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN,
                String.format("User with user name %s not found!", loggedInUserName)));

        if (user.getRoles().contains("ROLE_TENANT_ADMIN") && loggedInUser.getRoles().contains("ROLE_TENANT_ADMIN") == false) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Insufficient privileges to assign Role ROLE_TENANT_ADMIN!");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('ROLE_TENANT_ADMIN', 'ROLE_USER_ADMIN')")
    public void updateUser(User user) {
        validateTenantId(user.getUsername());
        if (Arrays.asList("ROLE_TENANT_ADMIN", "ROLE_USER_ADMIN", "ROLE_USER_READER", "ROLE_USER")
                .containsAll(user.getRoles()) == false) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid role found!");
        }

        User currentUser = userRepository.findById(user.getUsername()).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        String.format("User with username %s not found.", user.getUsername())));

        String loggedInUserName = AuthUtils.getCurrentQualifiedUsername();
        User loggedInUser = userRepository.findById(loggedInUserName).orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN,
                String.format("User with user name %s not found!", loggedInUserName)));

        //check if role ROLE_TENANT_ADMIN is assigned or removed
        // or if user already has ROLE_TENANT_ADMIN,
        // than logged in user should have role ROLE_TENANT_ADMIN.
        if ((user.getRoles().contains("ROLE_TENANT_ADMIN") || currentUser.getRoles().contains("ROLE_TENANT_ADMIN"))
                && loggedInUser.getRoles().contains("ROLE_TENANT_ADMIN") == false) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Insufficient privileges to assign Role ROLE_TENANT_ADMIN!");
        }

        user.setPassword(currentUser.getPassword()); //keep existing password as it is, do not update it.
        userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('ROLE_TENANT_ADMIN', 'ROLE_USER_ADMIN', 'ROLE_USER_READER')")
    public Optional<User> getUser(String userName) {
        validateTenantId(userName);
        return userRepository.findById(userName);
    }

    @Override
    @Transactional(readOnly = true)
    public User getLoggedInUserDetails() {
        String userName = AuthUtils.getCurrentQualifiedUsername();
        return userRepository.findById(userName).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("User with username %s not found.", userName)));
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('ROLE_TENANT_ADMIN', 'ROLE_USER_ADMIN')")
    public void updateUserStatus(String userName, boolean status) {
        validateTenantId(userName);
        User user = userRepository.findById(userName).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        String.format("User with username %s not found.", userName)));

        String loggedInUserName = AuthUtils.getCurrentQualifiedUsername();
        User loggedInUser = userRepository.findById(loggedInUserName).orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN,
                String.format("User with user name %s not found!", loggedInUserName)));

        if (user.getRoles().contains("ROLE_TENANT_ADMIN") && loggedInUser.getRoles().contains("ROLE_TENANT_ADMIN") == false) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Insufficient privileges, Role ROLE_TENANT_ADMIN required!");
        }

        if (user.isEnabled() == status) {
            return;
        }
        user.setEnabled(status);
        userRepository.save(user);
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('ROLE_TENANT_ADMIN', 'ROLE_USER_ADMIN')")
    public void updateUserDisplayName(String userName, String displayName) {
        validateTenantId(userName);
        User user = userRepository.findById(userName).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("User with username %s not found.", userName)));

        String loggedInUserName = AuthUtils.getCurrentQualifiedUsername();
        User loggedInUser = userRepository.findById(loggedInUserName)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN,
                        String.format("User with user name %s not found!", loggedInUserName)));
        if (user.getRoles().contains("ROLE_TENANT_ADMIN") && loggedInUser.getRoles().contains("ROLE_TENANT_ADMIN") == false) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Insufficient privileges, Role ROLE_TENANT_ADMIN required!");
        }

        user.setDisplayName(displayName);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void updateMyDisplayName(String displayName) {
        String userName = AuthUtils.getCurrentQualifiedUsername();
        User user = userRepository.findById(userName).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("User with username %s not found.", userName)));
        user.setDisplayName(displayName);
        userRepository.save(user);
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('ROLE_TENANT_ADMIN', 'ROLE_USER_ADMIN')")
    public void deleteUser(String userName) {
        validateTenantId(userName);
        User user = userRepository.findById(userName).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("User with username %s not found.", userName)));

        String loggedInUserName = AuthUtils.getCurrentQualifiedUsername();
        User loggedInUser = userRepository.findById(loggedInUserName)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN,
                        String.format("User with user name %s not found!", loggedInUserName)));
        if (user.getRoles().contains("ROLE_TENANT_ADMIN") && loggedInUser.getRoles().contains("ROLE_TENANT_ADMIN") == false) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Insufficient privileges, Role ROLE_TENANT_ADMIN required!");
        }

        userRepository.deleteById(userName);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('ROLE_TENANT_ADMIN', 'ROLE_USER_ADMIN', 'ROLE_USER_READER')")
    public List<User> listAllUsers() {
        return userRepository.findAllByUserNameEndsWith("@" + AuthUtils.getCurrentTenantId());
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('ROLE_TENANT_ADMIN', 'ROLE_USER_ADMIN')")
    public void addRole(String userName, String role) {
        validateTenantId(userName);

        if (Arrays.asList("ROLE_TENANT_ADMIN", "ROLE_USER_ADMIN", "ROLE_USER_READER", "ROLE_USER").contains(role) == false) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid role!");
        }

        User user = userRepository.findById(userName).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("User with username %s not found", userName)));

        String loggedInUserName = AuthUtils.getCurrentQualifiedUsername();
        User loggedInUser = userRepository.findById(loggedInUserName)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN,
                        String.format("User with user name %s not found!", loggedInUserName)));
        if (("ROLE_TENANT_ADMIN".equals(role) || user.getRoles().contains("ROLE_TENANT_ADMIN"))
                && loggedInUser.getRoles().contains("ROLE_TENANT_ADMIN") == false) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Insufficient privileges, Role ROLE_TENANT_ADMIN required!");
        }


        Set<String> userRoles = user.getRoles();
        if (userRoles.contains(role) == false) {
            userRoles.add(role);
            user.setRoles(userRoles);
            userRepository.save(user);
        }
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('ROLE_TENANT_ADMIN', 'ROLE_USER_ADMIN')")
    public void removeRole(String userName, String role) {
        validateTenantId(userName);

        if (Arrays.asList("ROLE_TENANT_ADMIN", "ROLE_USER_ADMIN", "ROLE_USER_READER", "ROLE_USER").contains(role) == false) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid role!");
        }

        User user = userRepository.findById(userName).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("User with username %s not found", userName)));

        String loggedInUserName = AuthUtils.getCurrentQualifiedUsername();
        User loggedInUser = userRepository.findById(loggedInUserName)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN,
                        String.format("User with user name %s not found!", loggedInUserName)));
        if (("ROLE_TENANT_ADMIN".equals(role) || user.getRoles().contains("ROLE_TENANT_ADMIN"))
                && loggedInUser.getRoles().contains("ROLE_TENANT_ADMIN") == false) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Insufficient privileges, Role ROLE_TENANT_ADMIN required!");
        }

        Set<String> userRoles = user.getRoles();
        if (userRoles.contains(role)) {
            userRoles.remove(role);
            user.setRoles(userRoles);
            userRepository.save(user);
        }
    }

    private void validateTenantId(String userName) {
        String currentTenantId = AuthUtils.getCurrentTenantId();
        String tenantIdInQualifiedUserName = StringUtils.substringAfterLast(userName, "@");
        if (!tenantIdInQualifiedUserName.equals(currentTenantId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format("Invalid Organization Id in fully qualified user name, expecting %s.", currentTenantId));
        }
    }

//    @Scheduled(cron = "${pipeline.cleanup.cron}")
//    @Transactional
//    public void cleanupPipelineJob() {
//        SecurityContext context = SecurityContextHolder.createEmptyContext();
//        Authentication authentication =
//                new UsernamePasswordAuthenticationToken("admin@zigmoi.com", "doesnotmatter", AuthorityUtils.createAuthorityList("ROLE_SUPER_ADMIN"));
//        context.setAuthentication(authentication);
//
//        SimpleAsyncTaskExecutor delegateExecutor = new SimpleAsyncTaskExecutor();
//        DelegatingSecurityContextExecutor executor = new DelegatingSecurityContextExecutor(delegateExecutor, context);
//
//        Runnable originalRunnable = new Runnable() {
//            public void run() {
//                System.out.println("test");
//               // System.out.println("count : " + releaseRepository.countAllByDeploymentResourceId("a0fcbb27-7e2f-44e0-b205-4b28249a7594"));
//                System.out.println("count: " + tenantService.listAllTenants().size());
//                //cleanPipelineResources(new ReleaseId("", ""));
//            }
//        };
//        executor.execute(originalRunnable);
//    }
}
