package org.zigmoi.ketchup.iam.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.zigmoi.ketchup.iam.dtos.UserResponseDto;
import org.zigmoi.ketchup.iam.dtos.UserRequestDto;
import org.zigmoi.ketchup.iam.entities.User;
import org.zigmoi.ketchup.iam.services.UserService;
import org.zigmoi.ketchup.common.validations.ValidDisplayName;

import javax.validation.Valid;
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

    @PostMapping
    public void createUser(@RequestBody @Valid UserRequestDto userRequestDto) {
        User user = new User();
        user.setUserName(userRequestDto.getUserName());
        user.setDisplayName(userRequestDto.getDisplayName());
        user.setPassword(userRequestDto.getPassword());
        user.setEmail(userRequestDto.getEmail());
        user.setFirstName(userRequestDto.getFirstName());
        user.setLastName(userRequestDto.getLastName());
        user.setEnabled(userRequestDto.isEnabled());
        user.setRoles(userRequestDto.getRoles());
        userService.createUser(user);
    }

    @PutMapping
    public void updateUser(@RequestBody @Valid UserRequestDto userRequestDto) {
        User user = new User();
        String userName = userRequestDto.getUserName();
        user.setUserName(userName);
        user.setDisplayName(userRequestDto.getDisplayName());
        user.setEmail(userRequestDto.getEmail());
        user.setFirstName(userRequestDto.getFirstName());
        user.setLastName(userRequestDto.getLastName());
        user.setEnabled(userRequestDto.isEnabled());
        user.setRoles(userRequestDto.getRoles());
        userService.updateUser(user);
    }

    @GetMapping("/{username}")
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
    public void deleteUser(@NotBlank @PathVariable("username") String userName) {
        userService.deleteUser(userName);
    }

    @PutMapping("/{username}/enable/{status}")
    public void updateUserStatus(@NotBlank @PathVariable("username") String userName, @NotBlank @PathVariable("status") boolean status) {
        userService.updateUserStatus(userName, status);
    }

    @PutMapping("/{username}/display-name/{display-name}")
    public void updateUserDisplayName(@NotBlank @PathVariable("username") String userName, @ValidDisplayName @PathVariable("display-name") String displayName) {
        userService.updateUserDisplayName(userName, displayName);
    }

    @PutMapping("/{username}/roles/{role-name}/add")
    public void addRole(@NotBlank @PathVariable("username") String userName, @NotBlank @PathVariable("role-name") String roleName) {
        userService.addRole(userName, roleName);
    }

    @PutMapping("/{username}/roles/{role-name}/remove")
    public void removeRole(@NotBlank @PathVariable("username") String userName, @NotBlank @PathVariable("role-name") String roleName) {
        userService.removeRole(userName, roleName);
    }

    @PutMapping("/my/display-name/{display-name}")
    public void updateMyDisplayName(@ValidDisplayName @PathVariable("display-name") String displayName) {
        userService.updateMyDisplayName(displayName);
    }

    @GetMapping
    public List<UserResponseDto> listUsers() {
        return userService.listAllUsers().stream()
                .map(user -> {
                    return prepareUserResponseDto(user);
                })
                .sorted(Comparator.comparing(UserResponseDto::getUserName))
                .collect(Collectors.toList());
    }

    @GetMapping("/{username}/roles")
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
