package telran.spring.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.DynamicTest.stream;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.test.context.TestPropertySource;

import jakarta.annotation.PreDestroy;
import telran.spring.security.model.Account;
import telran.spring.security.service.AccountProviderImpl;
import telran.spring.security.service.AccountServiceImpl;

@SpringBootTest(classes = {AccountProviderImpl.class,AccountServiceImpl.class, AccountingConfiguration.class})

@TestPropertySource(properties = {"app.security.admin.password=ppp", "app.security.admin.username=admin"})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AccountingServiceTest {

	@Autowired
	UserDetailsManager userDetailsManager;
	@Autowired
	AccountServiceImpl service;
	
	Account[] testAccounts = {
			new Account("user", "user", new String[] {"USER"}),
			new Account("user1", "user1", new String[] {"USER"}),
			new Account("user2", "user2", new String[] {"USER"})
	};
			
	@Test
	@Order(0)
	void adminExistsTest() {
		assertTrue(userDetailsManager.userExists("admin"));
	}
	
	@Test
	@Order(1)
	void addUserTest() {
		for (Account account : testAccounts) {
			service.addAccount(account);
			assertTrue(userDetailsManager.userExists(account.getUserName()));
		}	
	}
	

	@Test
	@Order(10)
	void deleteUserTest() {
		for (Account account : testAccounts) {
			service.deleteAccount(account.getUserName());
			assertFalse(userDetailsManager.userExists(account.getUserName()));
		}
		
	}
	
	

}
