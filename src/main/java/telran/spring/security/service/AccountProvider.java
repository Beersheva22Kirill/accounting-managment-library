package telran.spring.security.service;

import java.util.List;

import telran.spring.security.model.Account;

public interface AccountProvider {
	
	List<Account> getAccounts();
	void setAccounts(List<Account> accounts);

}
