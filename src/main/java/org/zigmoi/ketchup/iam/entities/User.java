package org.zigmoi.ketchup.iam.entities;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
<<<<<<< HEAD
import org.zigmoi.ketchup.project.entities.ProjectId;
=======
import org.zigmoi.ketchup.deployment.entities.DeploymentId;
import org.zigmoi.ketchup.globalsetting.entities.GlobalSettingId;
import org.zigmoi.ketchup.project.entities.ProjectId;
import org.zigmoi.ketchup.project.entities.ProjectSettingId;
>>>>>>> 7516c026e3957f51b7fb5836e18f8423d1ea584a

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "users")
public class User extends TenantEntity implements UserDetails {

    @Id
    @NotBlank(message = "Please provide fully qualified user name with Organization Id, example: user@organization-id.")
    private String userName; //fully qualified username user@tenant example: test@zigmoi.com
    // private String tenantId;
    private String password;
    private String displayName;
    private boolean enabled;
    private String email;
    private String firstName;
    private String lastName;


    @Temporal(TemporalType.TIMESTAMP)
    private Date creationDate;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "userName", referencedColumnName = "userName"))
    @Column(name = "role")
    Set<String> roles = new HashSet<>();

<<<<<<< HEAD
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "user_projects", joinColumns = @JoinColumn(name = "userName", referencedColumnName = "userName"))
    @Column(name = "project")
    Set<ProjectId> projects = new HashSet<>();

=======
//    @ElementCollection(fetch = FetchType.LAZY)
//    @CollectionTable(name = "user_projects", joinColumns = @JoinColumn(name = "userName", referencedColumnName = "userName"))
//    @Column(name = "project")
//    Set<ProjectId> projects = new HashSet<>();
//
//    @ElementCollection(fetch = FetchType.LAZY)
//    @CollectionTable(name = "user_global_settings", joinColumns = @JoinColumn(name = "userName", referencedColumnName = "userName"))
//    @Column(name = "global_setting")
//    Set<GlobalSettingId> globalSettings = new HashSet<>();
//
//    @ElementCollection(fetch = FetchType.LAZY)
//    @CollectionTable(name = "user_project_settings", joinColumns = @JoinColumn(name = "userName", referencedColumnName = "userName"))
//    @Column(name = "project_setting")
//    Set<ProjectSettingId> projectSettings = new HashSet<>();
//
>>>>>>> 7516c026e3957f51b7fb5836e18f8423d1ea584a
//    @ElementCollection(fetch = FetchType.LAZY)
//    @CollectionTable(name = "user_deployments", joinColumns = @JoinColumn(name = "userName", referencedColumnName = "userName"))
//    @Column(name = "deployment")
//    Set<DeploymentId> deployments = new HashSet<>();

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<GrantedAuthority> authorities = roles.stream().map(role -> {
            SimpleGrantedAuthority simpleGrantedAuthority = new SimpleGrantedAuthority(role);
            return simpleGrantedAuthority;
        }).collect(Collectors.toSet());
        return authorities;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public String getUsername() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }

<<<<<<< HEAD
    public Set<ProjectId> getProjects() {
        return projects;
    }

    public void setProjects(Set<ProjectId> projects) {
        this.projects = projects;
    }

=======
//    public Set<ProjectId> getProjects() {
//        return projects;
//    }
//
//    public void setProjects(Set<ProjectId> projects) {
//        this.projects = projects;
//    }
//
//    public Set<GlobalSettingId> getGlobalSettings() {
//        return globalSettings;
//    }
//
//    public void setGlobalSettings(Set<GlobalSettingId> globalSettings) {
//        this.globalSettings = globalSettings;
//    }
//
//    public Set<ProjectSettingId> getProjectSettings() {
//        return projectSettings;
//    }
//
//    public void setProjectSettings(Set<ProjectSettingId> projectSettings) {
//        this.projectSettings = projectSettings;
//    }
//
>>>>>>> 7516c026e3957f51b7fb5836e18f8423d1ea584a
//    public Set<DeploymentId> getDeployments() {
//        return deployments;
//    }
//
//    public void setDeployments(Set<DeploymentId> deployments) {
//        this.deployments = deployments;
//    }

}
