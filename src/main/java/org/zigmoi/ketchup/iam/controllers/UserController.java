package org.zigmoi.ketchup.iam.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.zigmoi.ketchup.common.ConfigUtility;
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
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/v1/user")
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

    @GetMapping("/v1/user/{username}")
    public UserDto getUser(@PathVariable("username") String userName) {
        User user = userService.getUser(userName).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("User with username %s not found", userName)));
        return prepareUserDto(user);
    }

    @GetMapping("/v1/user/my/profile")
    public UserDto getMyProfile() {
        User user = userService.getLoggedInUserDetails();
        return prepareUserDto(user);
    }

    @DeleteMapping("/v1/user/{username}")
    public void deleteUser(@PathVariable("username") String userName) {
        userService.deleteUser(userName);
    }

    @PutMapping("/v1/user/{username}/enable/{status}")
    public void updateUserStatus(@PathVariable("username") String userName, @PathVariable("status") boolean status) {
        userService.updateUserStatus(userName, status);
    }

    @PutMapping("/v1/user/{username}/displayName/{displayName}")
    public void updateUserDisplayName(@PathVariable("username") String userName, @PathVariable("displayName") String displayName) {
        userService.updateUserDisplayName(userName, displayName);
    }

    @PutMapping("/v1/user/{username}/role/{roleName}/add")
    public void addRole(@PathVariable("username") String userName, @PathVariable("roleName") String roleName) {
        userService.addRole(userName, roleName);
    }

    @PutMapping("/v1/user/{username}/role/{roleName}/remove")
    public void removeRole(@PathVariable("username") String userName, @PathVariable("roleName") String roleName) {
        userService.removeRole(userName, roleName);
    }

    @PutMapping("/v1/user/my/displayName/{displayName}")
    public void updateMyDisplayName(@PathVariable("displayName") String displayName) {
        userService.updateMyDisplayName(displayName);
    }

    @GetMapping("/v1/users")
    public List<UserDto> listUsers() {
        return userService.listAllUsers().stream()
                .map(user -> {
                    return prepareUserDto(user);
                })
                .sorted(Comparator.comparing(UserDto::getUserName))
                .collect(Collectors.toList());
    }

    @GetMapping("/v1/user/{username}/projects")
    public Set<String> listUserProjects(@PathVariable("username") String userName) {
        User user = userService.getUser(userName).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found."));
        return user.getProjects();
    }

    @GetMapping("/v1/user/{username}/roles")
    public Set<String> listUserRoles(@PathVariable("username") String userName) {
        User user = userService.getUser(userName).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("User with username %s not found", userName)));
        return user.getRoles();
    }

//    @GetMapping("/v1/user/{username}/globalSettings")
//    public Set<GlobalSettingId> listUserGlobalSettings(@PathVariable("username") String userName) {
//        return userService.getUser(userName).get().getGlobalSettings();
//    }
//
//    @GetMapping("/v1/user/{username}/project/{projectId}/projectSettings")
//    public Set<ProjectSettingId> listUserProjectSettings(@PathVariable("username") String userName, @PathVariable("projectId") String projectId) {
//        //filter projects in memory or in sql based on projectId.
//        return userService.getUser(userName).get().getProjectSettings();
//    }
//
//    @GetMapping("/v1/user/{username}/project/{projectId}/deployments")
//    public Set<DeploymentId> listUserDeployments(@PathVariable("username") String userName, @PathVariable("projectId") String projectId) {
//        //filter projects in memory or in sql based on projectId.
//        return userService.getUser(userName).get().getDeployments();
//    }

    //add and remove methods for projects, deployments, project settings and global settings.

    public UserDto prepareUserDto(User user) {
        UserDto userDto = new UserDto();
        userDto.setUserName(user.getUsername());
        userDto.setDisplayName(user.getDisplayName());
        userDto.setEnabled(user.isEnabled());
        userDto.setEmail(user.getEmail());
        userDto.setFirstName(user.getFirstName());
        userDto.setLastName(user.getLastName());
        userDto.setCreationDate(user.getCreationDate());
        userDto.setRoles(user.getRoles());
        return userDto;
    }

    @GetMapping("/v1/getProp")
    public String getProp(@RequestParam String key) {
        return ConfigUtility.instance().getProperty(key);
    }
}
