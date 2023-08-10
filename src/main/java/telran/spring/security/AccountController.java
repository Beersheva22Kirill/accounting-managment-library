package telran.spring.security;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import telran.spring.security.model.Account;
import telran.spring.security.service.AccountService;


@RestController
@RequestMapping("accounts")
@RequiredArgsConstructor
@Slf4j
public class AccountController {

	final AccountService accountService;
	
	@GetMapping("{userName}")
	public Account getAccount(@PathVariable String userName) {
		
		return accountService.getAccount(userName);
	}


	@PostMapping
	public void addAccount(@RequestBody Account account) {
		log.debug("Controller recieved account: " + account.toString());
		accountService.addAccount(account);
	}


	@PutMapping("userName")
	public void updatePassword(@PathVariable String userName, @RequestBody String newPassword) {
		
		accountService.updatePassword(userName, newPassword);

	}


	@DeleteMapping("{userName}")
	public void deleteAccount(@PathVariable String userName) {
		
		accountService.deleteAccount(userName);

	}

}
