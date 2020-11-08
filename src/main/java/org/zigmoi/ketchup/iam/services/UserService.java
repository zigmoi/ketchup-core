package org.zigmoi.ketchup.iam.services;

import org.springframework.validation.annotation.Validated;
import org.zigmoi.ketchup.iam.entities.User;
import org.zigmoi.ketchup.common.validations.ValidDisplayName;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;

@Validated
public interface UserService {


    void createUser(@Valid User user);

    void updateUser(@Valid User user);

    Optional<User> getUser(@NotBlank String userName);

    User getLoggedInUserDetails();

    void updateUserStatus(@NotBlank String userName, @NotNull boolean status);

    void updateUserDisplayName(@NotBlank String userName, @ValidDisplayName String displayName);

    void updateMyDisplayName(@ValidDisplayName String displayName);

    void deleteUser(@NotBlank String userName);

    List<User> listAllUsers();

    void addRole(@NotBlank String userName, @NotBlank String role);

    void removeRole(@NotBlank String userName, @NotBlank String role);
}