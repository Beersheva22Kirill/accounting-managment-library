package telran.spring;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import telran.spring.security.model.Account;
import telran.spring.security.service.AccountService;
import telran.spring.security.validation.PasswordValidator;


@RestController
@RequestMapping("accounts")
@RequiredArgsConstructor
@Slf4j
public class AccountController implements AccountService{

	final AccountService accountService;
	final PasswordValidator passwordValidator;
	
	@Override
	@GetMapping("{userName}")
	public Account getAccount(@PathVariable String userName) {
		
		return accountService.getAccount(userName);
	}

	@Override
	@PostMapping
	public void addAccount(@RequestBody @Valid Account account) {
		log.debug("Controller recieved account: " + account.toString());
		passwordValidator.validate(account.getPassword());
		accountService.addAccount(account);
	}

	@Override
	@PutMapping("{userName}")
	public void updatePassword(@PathVariable String userName, @RequestBody String newPassword) {
		passwordValidator.validate(newPassword);
		accountService.updatePassword(userName, newPassword);

	}
	
	
	@Override
	@DeleteMapping("{userName}")
	public void deleteAccount(@PathVariable String userName) {
		
		accountService.deleteAccount(userName);

	}

}
