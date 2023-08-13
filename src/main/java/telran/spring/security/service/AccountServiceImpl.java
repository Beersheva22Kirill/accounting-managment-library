package telran.spring.security.service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import telran.spring.exceptions.NotFoundException;
import telran.spring.security.model.Account;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountServiceImpl implements AccountService {
	
	final PasswordEncoder passwordEncoder;
	@Value("${app.expiration.period:600}")
    String expirationPeriodHours;
	@Value("${app.security.passwords.limit:3}")
	int limitPasswords;
	@Value("${app.security.validation.period:3600000}")
	long validationPeriod;
	
	final AccountProvider provider;
	@Autowired
	UserDetailsManager detailsService;
	
	ConcurrentHashMap<String, Account> map = new ConcurrentHashMap<String, Account>();
	
	
	@Override
	public Account getAccount(String userName) {
		Account account = map.get(userName);
		if(account == null) {
			throw new NotFoundException("Account with username:" + userName + " not found");
		}		
		return account;
		
	}

	@Override
	public void addAccount(Account account) {
		if(map.containsKey(account.getUserName())) {
			log.error("User with username {} exists",account.getUserName());
			throw new RuntimeException();
		}
			LocalDateTime expPeriod = LocalDateTime.now().plus(Long.valueOf(expirationPeriodHours), ChronoUnit.HOURS);
			String hashPass = passwordEncoder.encode(account.getPassword());
			Account newAccount = new Account(account.getUserName(), hashPass, account.getRoles());
			LinkedList<String> passwords = new LinkedList<String>();
			passwords.add(hashPass);
			newAccount.setOldPass(passwords);
			newAccount.setExpDate(expPeriod);
			createUser(newAccount);		
	}

	private void createUser(Account accountForAdd) {
		map.put(accountForAdd.getUserName(), accountForAdd);
		detailsService.createUser(User.withUsername(accountForAdd.getUserName())
				.password(accountForAdd.getPassword()).roles(accountForAdd.getRoles())
				.accountExpired(LocalDateTime.now().compareTo(accountForAdd.getExpDate()) >= 0) .build());
		log.debug("User with username {} added", accountForAdd.getUserName());
	}

	@Override
	public void updatePassword(String userName, String newPassword) {
		Account oldAcc = map.get(userName);
		String username = oldAcc.getUserName();
		String[] roles = oldAcc.getRoles();
		
		if(oldAcc == null) {
			log.error("Account with username {} for update not found");
			throw new NotFoundException(String.format("username $s not found", userName));
		}
		
		if(oldAcc.getOldPass().stream().anyMatch(hash -> passwordEncoder.matches(newPassword, hash))) {
			log.error("Missmath Passwords Strategy");
			throw new IllegalStateException("Missmath Passwords Strategy");
		}
		
		LinkedList<String> passwords = oldAcc.getOldPass();
		String hashPassword = passwordEncoder.encode(newPassword);
		
		if(passwords.size() == limitPasswords) {
			passwords.removeFirst();
		}
		
		passwords.add(hashPassword);	
		Account newAcc = new Account(username, hashPassword, roles);
		newAcc.setOldPass(passwords);
		LocalDateTime expDate = LocalDateTime.now().plus(Long.valueOf(expirationPeriodHours), ChronoUnit.HOURS);
		newAcc.setExpDate(expDate);
		map.put(username, newAcc);
		detailsService.updateUser(User.withUsername(username).password(hashPassword).roles(roles)
				.accountExpired(LocalDateTime.now().compareTo(expDate) >= 0)
				.build());
		log.debug("Account with username {} updated", userName);

	}

	@Override
	public void deleteAccount(String userName) {
		Account removed = map.remove(userName);
		if (removed == null) {
			throw new NotFoundException("Account with username " + userName + " not found" );
		}
		detailsService.deleteUser(userName);
		log.debug("Account with username {} deleted", userName);	
	}
	
	@PostConstruct
	void restoreAccounts() {
	
		List<Account> accounts = provider.getAccounts();
		for (Account account : accounts) {
				createUser(account);	
		}
		
		Thread thread = new Thread(() -> {
			while(true) {
					try {
						Thread.sleep(validationPeriod);
					} catch (InterruptedException e) {
						
					}
					expirationValidation();	
					log.info("");
			}
		});
		thread.setDaemon(true);
		thread.start();
	}
	
	@PreDestroy
	void saveAccounts(){
		List<Account> accToSave = new LinkedList<Account>(map.values());
		provider.setAccounts(accToSave);
	}
	
	//@Scheduled(fixedDelay = 1,timeUnit = TimeUnit.HOURS)
	void expirationValidation() {
		int[] count = {0};
		map.values().stream().filter(this::isExpired).forEach(a -> {
			log.debug("account {} expired", a);
			detailsService.updateUser(User.withUsername(a.getUserName()).password(a.getPassword())
						.accountExpired(true).build());
				count[0]++;
		});
		log.debug("expiration validation {} accounts have been expired", count[0]);	
			
	}

	private boolean isExpired(Account account) {
		
		return LocalDateTime.now().isBefore(account.getExpDate());
	}

}
