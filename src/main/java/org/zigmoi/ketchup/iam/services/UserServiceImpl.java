package org.zigmoi.ketchup.iam.services;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.server.ResponseStatusException;
import org.zigmoi.ketchup.iam.annotations.TenantFilter;
import org.zigmoi.ketchup.project.entities.ProjectId;
import org.zigmoi.ketchup.iam.common.AuthUtils;
import org.zigmoi.ketchup.iam.entities.Tenant;
import org.zigmoi.ketchup.iam.entities.User;
import org.zigmoi.ketchup.iam.exceptions.TenantInActiveException;
import org.zigmoi.ketchup.iam.exceptions.TenantNotFoundException;
import org.zigmoi.ketchup.iam.repositories.UserRepository;

import javax.transaction.Transactional;
import javax.validation.Valid;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;


@Service("userDetailsService")
public class UserServiceImpl extends TenantProviderService implements UserDetailsService, UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TenantService tenantService;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;


    @Override
    public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {
        Assert.hasLength(userName, "UserName cannot be empty.");
        //Get user from db.
        User user = userRepository.findById(userName)
                .orElseThrow(() -> new UsernameNotFoundException("User account can not be located!"));

        //Check tenant active for this user.
        Tenant tenant = tenantService.getTenant(user.getTenantId()).orElseThrow(() -> new TenantNotFoundException(String.format("Tenant not found for user %s", user.getUsername())));
        if (tenant.isEnabled() == false) {
            throw new TenantInActiveException(String.format("Tenant not active for user %s", user.getUsername()));
        }

        return user;
    }

    @Override
    @Transactional
    public void createUser(@Valid User user) {
        if (userRepository.findById(user.getUsername()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format("User with user name %s already exists!", user.getUsername()));
        }

        if (AuthUtils.matchesPolicy(user.getPassword()) == false) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format("User Password %s is invalid!", user.getPassword()));
        }

        String currentTenantId = AuthUtils.getCurrentTenantId();
        String tenantIdInQualifiedUserName = StringUtils.substringAfterLast(user.getUsername(), "@");
        if (tenantIdInQualifiedUserName.equals(currentTenantId) == false) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format("Invalid Organization Id in fully qualified user name, expecting %s.", currentTenantId));
        }

        user.setCreationDate(new Date());
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
    }

    @Override
    @Transactional
    @TenantFilter
    public Optional<User> getUser(String userName) {
        return userRepository.findById(userName);
    }

    @Override
    @Transactional
    public void updateUserStatus(String userName, boolean status) {
        User user = userRepository.findById(userName).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("User with username %s not found.", userName)));
        if (user.isEnabled() == status) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("User with username %s has same status as requested.", userName));
        }
        user.setEnabled(status);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void updateUserDisplayName(String userName, String displayName) {
        User user = userRepository.findById(userName).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("User with username %s not found.", userName)));
        user.setDisplayName(displayName);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void deleteUser(String userName) {
        if (userRepository.findById(userName).isPresent()) {
            userRepository.deleteById(userName);
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("User with username %s not found", userName));
        }
    }

    @Override
    @TenantFilter
    @Transactional
    public List<User> listAllUsers() {
        return userRepository.findAll();
    }

//    @Override
//    @Transactional
//    public boolean verifyProjectExists(String userName, ProjectId projectId) {
//        return userRepository.existsByUserNameAndProjectsExists(userName, projectId);
//    }

    @Override
    @Transactional
    public void addProject(String userName, ProjectId projectId) {
        User user = userRepository.findById(userName).get();
        Set<ProjectId> userProjects = user.getProjects();
        if (userProjects.contains(projectId) == false) {
            userProjects.add(projectId);
            user.setProjects(userProjects);
            userRepository.save(user);
        }
    }

    @Override
    @Transactional
    public void removeProject(String userName, ProjectId projectId) {
        User user = userRepository.findById(userName).get();
        Set<ProjectId> userProjects = user.getProjects();
        if (userProjects.contains(projectId)) {
            userProjects.remove(projectId);
            user.setProjects(userProjects);
            userRepository.save(user);
        }
    }
}