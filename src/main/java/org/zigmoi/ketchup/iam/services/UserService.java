package org.zigmoi.ketchup.iam.services;

import org.springframework.security.access.prepost.PreAuthorize;
import org.zigmoi.ketchup.iam.entities.User;

import java.util.List;
import java.util.Optional;

public interface UserService {


    void createUser(User user);

    Optional<User> getUser(String userName);

    User getLoggedInUserDetails();

    void updateUserStatus(String userName, boolean status);

    void updateUserDisplayName(String userName, String displayName);

    void updateMyDisplayName(String displayName);

    void deleteUser(String userName);

    List<User> listAllUsers();

    void addProject(String userName, String projectResourceId);

    void removeProject(String userName, String projectResourceId);
}