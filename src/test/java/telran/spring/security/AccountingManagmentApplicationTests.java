package telran.spring.security;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import telran.spring.security.model.Account;
import telran.spring.security.service.AccountProviderImpl;
import telran.spring.security.service.AccountServiceImpl;

@SpringBootTest(classes = {AccountProviderImpl.class,AccountController.class, Account.class,AccountServiceImpl.class})
class AccountingManagmentApplicationTests {

	@Test
	void contextLoads() {
	}

}
