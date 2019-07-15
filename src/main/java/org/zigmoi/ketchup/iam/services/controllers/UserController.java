package org.zigmoi.ketchup.iam.services.controllers;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.zigmoi.ketchup.iam.entities.User;
import org.zigmoi.ketchup.iam.repositories.UserRepository;

@RestController
public class UserController {

	private static final Log logger = LogFactory.getLog(UserController.class);

	@Autowired
	private UserRepository userRepository;

	@GetMapping("/users")
	public List<User> listUsers() {
		return userRepository.findAll();
	}

}
