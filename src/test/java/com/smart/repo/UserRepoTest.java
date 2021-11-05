package com.smart.repo;

import static org.assertj.core.api.Assertions.assertThat;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.smart.dao.UserRepository;
import com.smart.entities.User;


@SpringBootTest
public class UserRepoTest {
	
	@Autowired
	private UserRepository userRepository;
	
	@Test
	public void saveUserTest() {
		User user = new User(39, "Balloon", "balloon@gmail.com", "balloon", true);
		userRepository.save(user);
		Assertions.assertThat(user.getId()).isGreaterThan(0);
		
	}
	
	@AfterEach
	void tearDown() {
		userRepository.deleteById(39);
	}
}
