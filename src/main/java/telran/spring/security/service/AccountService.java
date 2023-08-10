package telran.spring.security.service;

import telran.spring.security.model.Account;

public interface AccountService {
	
	Account getAccount(String userName);
	void addAccount(Account account);
	void updatePassword(String userName, String newPassword);
	void deleteAccount(String userName);

}
