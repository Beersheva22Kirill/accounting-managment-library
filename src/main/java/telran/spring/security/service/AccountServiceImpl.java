package telran.spring.security.service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
	@Value("${app.security.admin.password}")
	String adminPassword;
	@Value("${app.security.admin.name:admin}")
	String adminUsername;
	
	final AccountProvider provider;
	@Autowired
	UserDetailsManager detailsService;
	
	HashMap<String, Account> map = new HashMap<String, Account>();
	
	@Bean
	UserDetailsManager getUserDetailsService() {
		UserDetailsManager manager = new InMemoryUserDetailsManager();
		manager.createUser(User.withUsername(adminUsername)
				.password(passwordEncoder.encode(adminPassword))
				.roles("ADMIN").build());
		return manager;
	}
	
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
		if(account.getExpDate() == null) {
			LocalDateTime expPeriod = LocalDateTime.now().plus(Long.valueOf(expirationPeriodHours), ChronoUnit.HOURS);
			Account newAccount = new Account(account.getUserName(), passwordEncoder.encode(account.getPassword()), account.getRoles());
			newAccount.setExpDate(expPeriod);
			addToMapAndDetailService(newAccount);
		} else {
			addToMapAndDetailService(account);
		}
		
	}

	private void addToMapAndDetailService(Account accountForAdd) {
		map.put(accountForAdd.getUserName(), accountForAdd);
		detailsService.createUser(User.withUsername(accountForAdd.getUserName())
				.password(accountForAdd.getPassword()).roles(accountForAdd.getRoles()).build());
	}

	@Override
	public void updatePassword(String userName, String newPassword) {
		Account oldAcc = map.get(userName);
		Account newAcc = new Account(oldAcc.getUserName(), newPassword, oldAcc.getRoles());
		deleteAccount(userName);
		addAccount(newAcc);

	}

	@Override
	public void deleteAccount(String userName) {
		detailsService.deleteUser(userName);
		map.remove(userName);
	}
	
	@PostConstruct
	void restoreAccounts() {
		LocalDateTime now = LocalDateTime.now();
		List<Account> accounts = provider.getAccounts();
		for (Account account : accounts) {
			if (now.isBefore(account.getExpDate())) {
				addAccount(account);
			}
			log.debug("Account with name: {} expired ", account.getUserName());
		}
	}
	
	@PreDestroy
	void saveAccounts(){
		List<Account> accToSave = new ArrayList<Account>(map.values());
		provider.setAccounts(accToSave);
	}
	
	@Scheduled(fixedDelay = 1,timeUnit = TimeUnit.HOURS)
	void expirationValidation() {
		LocalDateTime now = LocalDateTime.now();
		List<Account> accounts = new ArrayList<Account>(map.values());
		for (Account account : accounts) {
			if(now.isBefore(account.getExpDate())) {
				detailsService.deleteUser(account.getUserName());
			}
		}
		
	}

}
