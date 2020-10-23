package org.zigmoi.ketchup.iam.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.zigmoi.ketchup.iam.dtos.UserDto;
import org.zigmoi.ketchup.iam.dtos.UserRequestDto;
import org.zigmoi.ketchup.iam.entities.User;
import org.zigmoi.ketchup.iam.services.UserService;

import javax.validation.Valid;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
    public UserDto getUser(@PathVariable("username") String userName) {
        User user = userService.getUser(userName).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("User with username %s not found", userName)));
        return prepareUserDto(user);
    }

    @GetMapping("/my/profile")
    public UserDto getMyProfile() {
        User user = userService.getLoggedInUserDetails();
        return prepareUserDto(user);
    }

    @DeleteMapping("/{username}")
    public void deleteUser(@PathVariable("username") String userName) {
        userService.deleteUser(userName);
    }

    @PutMapping("/{username}/enable/{status}")
    public void updateUserStatus(@PathVariable("username") String userName, @PathVariable("status") boolean status) {
        userService.updateUserStatus(userName, status);
    }

    @PutMapping("/{username}/displayName/{displayName}")
    public void updateUserDisplayName(@PathVariable("username") String userName, @PathVariable("displayName") String displayName) {
        userService.updateUserDisplayName(userName, displayName);
    }

    @PutMapping("/{username}/roles/{roleName}/add")
    public void addRole(@PathVariable("username") String userName, @PathVariable("roleName") String roleName) {
        userService.addRole(userName, roleName);
    }

    @PutMapping("/{username}/roles/{roleName}/remove")
    public void removeRole(@PathVariable("username") String userName, @PathVariable("roleName") String roleName) {
        userService.removeRole(userName, roleName);
    }

    @PutMapping("/my/displayName/{displayName}")
    public void updateMyDisplayName(@PathVariable("displayName") String displayName) {
        userService.updateMyDisplayName(displayName);
    }

    @GetMapping
    public List<UserDto> listUsers() {
        return userService.listAllUsers().stream()
                .map(user -> {
                    return prepareUserDto(user);
                })
                .sorted(Comparator.comparing(UserDto::getUserName))
                .collect(Collectors.toList());
    }

    @GetMapping("/{username}/roles")
    public Set<String> listUserRoles(@PathVariable("username") String userName) {
        User user = userService.getUser(userName).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("User with username %s not found", userName)));
        return user.getRoles();
    }

    public UserDto prepareUserDto(User user) {
        UserDto userDto = new UserDto();
        userDto.setUserName(user.getUsername());
        userDto.setDisplayName(user.getDisplayName());
        userDto.setEnabled(user.isEnabled());
        userDto.setEmail(user.getEmail());
        userDto.setFirstName(user.getFirstName());
        userDto.setLastName(user.getLastName());
        userDto.setCreatedOn(user.getCreatedOn());
        userDto.setCreatedBy(user.getCreatedBy());
        userDto.setLastUpdatedOn(user.getLastUpdatedOn());
        userDto.setLastUpdatedBy(user.getLastUpdatedBy());
        userDto.setRoles(user.getRoles());
        return userDto;
    }
}
