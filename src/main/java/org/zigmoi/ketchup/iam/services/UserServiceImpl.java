package org.zigmoi.ketchup.iam.services;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.web.server.ResponseStatusException;
import org.zigmoi.ketchup.iam.commons.AuthUtils;
import org.zigmoi.ketchup.iam.entities.Tenant;
import org.zigmoi.ketchup.iam.entities.User;
import org.zigmoi.ketchup.iam.exceptions.TenantInActiveException;
import org.zigmoi.ketchup.iam.exceptions.TenantNotFoundException;
import org.zigmoi.ketchup.iam.repositories.UserRepository;

import javax.validation.Valid;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;


@Service("userDetailsService")
public class UserServiceImpl implements UserDetailsService, UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TenantService tenantService;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;


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
    public void createUser(@Valid User user) {
        validateTenantId(user.getUsername());

        if (userRepository.findById(user.getUsername()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format("User with user name %s already exists!", user.getUsername()));
        }

        if (!AuthUtils.matchesPolicy(user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format("User Password %s is invalid!", user.getPassword()));
        }

//        String currentUserName = AuthUtils.getCurrentQualifiedUsername();
//        User currentUser = userRepository.findById(currentUserName).orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN,
//                String.format("User with user name %s not found!", currentUserName)));
//
//        for (String role: user.getRoles()) {
//            if("ROLE_SUPER_ADMIN".equalsIgnoreCase()){}
//        }
//        currentUser.getRoles().contains("ROLE_")

        user.setCreationDate(new Date());
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('ROLE_TENANT_ADMIN', 'ROLE_USER_ADMIN')")
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
                new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("User with username %s not found.", userName)));
        if (user.isEnabled() == status) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("User with username %s has same status as requested.", userName));
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
        if (userRepository.findById(userName).isPresent()) {
            userRepository.deleteById(userName);
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("User with username %s not found", userName));
        }
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('ROLE_TENANT_ADMIN', 'ROLE_USER_ADMIN')")
    public List<User> listAllUsers() {
        return userRepository.findAllByUserNameEndsWith("@" + AuthUtils.getCurrentTenantId());
    }

//    @Override
//    @Transactional
//    public boolean verifyProjectExists(String userName, ProjectId projectId) {
//        return userRepository.existsByUserNameAndProjectsExists(userName, projectId);
//    }

    @Override
    @Transactional
    public void addProject(String userName, String projectResourceId) {
        validateTenantId(userName);
        User user = userRepository.findById(userName).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("User with username %s not found", userName)));
        Set<String> userProjects = user.getProjects();
        if (userProjects.contains(projectResourceId)) {
            userProjects.add(projectResourceId);
            user.setProjects(userProjects);
            userRepository.save(user);
        }
    }

    @Override
    @Transactional
    public void removeProject(String userName, String projectResourceId) {
        validateTenantId(userName);
        User user = userRepository.findById(userName).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("User with username %s not found", userName)));
        Set<String> userProjects = user.getProjects();
        if (userProjects.contains(projectResourceId)) {
            userProjects.remove(projectResourceId);
            user.setProjects(userProjects);
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
}
