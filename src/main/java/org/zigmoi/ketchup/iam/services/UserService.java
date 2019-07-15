package org.zigmoi.ketchup.iam.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.zigmoi.ketchup.iam.entities.User;
import org.zigmoi.ketchup.iam.repositories.UserRepository;



@Service("userDetailsService")
public class UserService implements UserDetailsService {

	@Autowired
	private UserRepository userRepository;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		Assert.hasLength(username, "Username cannot be empty.");
		User user = userRepository.findById(username)
				.orElseThrow(() -> new UsernameNotFoundException("Bamboo account can not be located!"));
		return user;
	}

	public Boolean matchesPolicy(String passwd) {
		String pattern = "(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&*])(?=\\S+$).{8,}";
		return passwd.matches(pattern);
	}
}