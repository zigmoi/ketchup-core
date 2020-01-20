package org.zigmoi.ketchup.iam.services;

import org.zigmoi.ketchup.iam.entities.User;

import java.util.List;
import java.util.Optional;

public interface UserService {


    void createUser(User user);

    void updateUser(User user);

    Optional<User> getUser(String userName);

    User getLoggedInUserDetails();

    void updateUserStatus(String userName, boolean status);

    void updateUserDisplayName(String userName, String displayName);

    void updateMyDisplayName(String displayName);

    void deleteUser(String userName);

    List<User> listAllUsers();

    void addRole(String userName, String role);

    void removeRole(String userName, String role);
}