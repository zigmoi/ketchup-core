package org.zigmoi.ketchup.iam.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.zigmoi.ketchup.application.dtos.ApplicationRequestDto;
import org.zigmoi.ketchup.iam.dtos.UserResponseDto;
import org.zigmoi.ketchup.iam.dtos.UserCreateRequestDto;
import org.zigmoi.ketchup.iam.dtos.UserUpdateRequestDto;
import org.zigmoi.ketchup.iam.entities.User;
import org.zigmoi.ketchup.iam.services.UserService;
import org.zigmoi.ketchup.common.validations.ValidDisplayName;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Valid;
import javax.validation.Validator;
import javax.validation.constraints.NotBlank;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Validated
@Slf4j
@RestController
@RequestMapping("/v1-alpha/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private Validator validator;

    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_TENANT_ADMIN', 'ROLE_USER_ADMIN')")
    public void createUser(@RequestBody @Valid UserCreateRequestDto userCreateRequestDto) {
        User user = new User();
        user.setUserName(userCreateRequestDto.getUserName());
        user.setDisplayName(userCreateRequestDto.getDisplayName());
        user.setPassword(userCreateRequestDto.getPassword());
        user.setEmail(userCreateRequestDto.getEmail());
        user.setFirstName(userCreateRequestDto.getFirstName());
        user.setLastName(userCreateRequestDto.getLastName());
        user.setEnabled(userCreateRequestDto.isEnabled());
        user.setRoles(userCreateRequestDto.getRoles());
        userService.createUser(user);
    }

    @PutMapping("/{username}")
    @PreAuthorize("hasAnyRole('ROLE_TENANT_ADMIN', 'ROLE_USER_ADMIN')")
    public void updateUser(@NotBlank @PathVariable("username") String userName,
                           @RequestBody UserUpdateRequestDto userUpdateRequestDto) {
        Set<ConstraintViolation<UserUpdateRequestDto>> violations = validator.validate(userUpdateRequestDto);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
        userService.updateUser(userName, userUpdateRequestDto);
    }

    @GetMapping("/{username}")
    @PreAuthorize("hasAnyRole('ROLE_TENANT_ADMIN', 'ROLE_USER_ADMIN', 'ROLE_USER_READER')")
    public UserResponseDto getUser(@NotBlank @PathVariable("username") String userName) {
        User user = userService.getUser(userName).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("User with username %s not found", userName)));
        return prepareUserResponseDto(user);
    }

    @GetMapping("/my/profile")
    public UserResponseDto getMyProfile() {
        User user = userService.getLoggedInUserDetails();
        return prepareUserResponseDto(user);
    }

    @DeleteMapping("/{username}")
    @PreAuthorize("hasAnyRole('ROLE_TENANT_ADMIN', 'ROLE_USER_ADMIN')")
    public void deleteUser(@NotBlank @PathVariable("username") String userName) {
        userService.deleteUser(userName);
    }

    @PutMapping("/{username}/enable/{status}")
    @PreAuthorize("hasAnyRole('ROLE_TENANT_ADMIN', 'ROLE_USER_ADMIN')")
    public void updateUserStatus(@NotBlank @PathVariable("username") String userName, @NotBlank @PathVariable("status") boolean status) {
        userService.updateUserStatus(userName, status);
    }

    @PutMapping("/{username}/display-name/{display-name}")
    @PreAuthorize("hasAnyRole('ROLE_TENANT_ADMIN', 'ROLE_USER_ADMIN')")
    public void updateUserDisplayName(@NotBlank @PathVariable("username") String userName, @ValidDisplayName @PathVariable("display-name") String displayName) {
        userService.updateUserDisplayName(userName, displayName);
    }

    @PutMapping("/{username}/roles/{role-name}/add")
    @PreAuthorize("hasAnyRole('ROLE_TENANT_ADMIN', 'ROLE_USER_ADMIN')")
    public void addRole(@NotBlank @PathVariable("username") String userName, @NotBlank @PathVariable("role-name") String roleName) {
        userService.addRole(userName, roleName);
    }

    @PutMapping("/{username}/roles/{role-name}/remove")
    @PreAuthorize("hasAnyRole('ROLE_TENANT_ADMIN', 'ROLE_USER_ADMIN')")
    public void removeRole(@NotBlank @PathVariable("username") String userName, @NotBlank @PathVariable("role-name") String roleName) {
        userService.removeRole(userName, roleName);
    }

    @PutMapping("/my/display-name/{display-name}")
    public void updateMyDisplayName(@ValidDisplayName @PathVariable("display-name") String displayName) {
        userService.updateMyDisplayName(displayName);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_TENANT_ADMIN', 'ROLE_USER_ADMIN', 'ROLE_USER_READER')")
    public List<UserResponseDto> listUsers() {
        return userService.listAllUsers().stream()
                .map(user -> {
                    return prepareUserResponseDto(user);
                })
                .sorted(Comparator.comparing(UserResponseDto::getUserName))
                .collect(Collectors.toList());
    }

    @GetMapping("/{username}/roles")
    @PreAuthorize("hasAnyRole('ROLE_TENANT_ADMIN', 'ROLE_USER_ADMIN', 'ROLE_USER_READER')")
    public Set<String> listUserRoles(@NotBlank @PathVariable("username") String userName) {
        User user = userService.getUser(userName).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("User with username %s not found", userName)));
        return user.getRoles();
    }

    public UserResponseDto prepareUserResponseDto(User user) {
        UserResponseDto userResponseDto = new UserResponseDto();
        userResponseDto.setUserName(user.getUsername());
        userResponseDto.setDisplayName(user.getDisplayName());
        userResponseDto.setEnabled(user.isEnabled());
        userResponseDto.setEmail(user.getEmail());
        userResponseDto.setFirstName(user.getFirstName());
        userResponseDto.setLastName(user.getLastName());
        userResponseDto.setCreatedOn(user.getCreatedOn());
        userResponseDto.setCreatedBy(user.getCreatedBy());
        userResponseDto.setLastUpdatedOn(user.getLastUpdatedOn());
        userResponseDto.setLastUpdatedBy(user.getLastUpdatedBy());
        userResponseDto.setRoles(user.getRoles());
        return userResponseDto;
    }
}
