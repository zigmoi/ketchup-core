package org.zigmoi.ketchup.iam.entities;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.Filter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.zigmoi.ketchup.project.entities.ProjectId;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
public class User implements UserDetails {

    @Id
    @NotBlank(message = "Please provide fully qualified user name with Organization Id, example: user@organization-id.")
    private String userName; //fully qualified username user@tenant example: test@zigmoi.com
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

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "user_projects", joinColumns = @JoinColumn(name = "userName", referencedColumnName = "userName"))
    @Column(name = "project_resource_id")
    Set<String> projects = new HashSet<>();

//    @ElementCollection(fetch = FetchType.LAZY)
//    @CollectionTable(name = "user_deployments", joinColumns = @JoinColumn(name = "userName", referencedColumnName = "userName"))
//    @Column(name = "deployment")
//    Set<DeploymentId> deployments = new HashSet<>();

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<SimpleGrantedAuthority> set = new HashSet<>();
        for (String role : roles) {
            SimpleGrantedAuthority simpleGrantedAuthority = new SimpleGrantedAuthority(role);
            set.add(simpleGrantedAuthority);
        }
        return set;
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
        return StringUtils.substringAfterLast(this.userName, "@");
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

    public Set<String> getProjects() {
        return projects;
    }

    public void setProjects(Set<String> projects) {
        this.projects = projects;
    }

//    public Set<DeploymentId> getDeployments() {
//        return deployments;
//    }
//
//    public void setDeployments(Set<DeploymentId> deployments) {
//        this.deployments = deployments;
//    }

}
