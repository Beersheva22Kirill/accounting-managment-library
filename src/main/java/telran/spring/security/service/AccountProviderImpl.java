package telran.spring.security.service;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import telran.spring.security.model.Account;
@Slf4j
@Service
public class AccountProviderImpl implements AccountProvider {
	@Value("${app.security.accounts.file.name:Accounts.data}")
	String fileName;
	
	@SuppressWarnings("unchecked")
	@Override
	public List<Account> getAccounts() {
		log.info("Begin of restore from:" + fileName);
		List<Account> accounts = new ArrayList<Account>();
		try(ObjectInputStream ois = new ObjectInputStream(new FileInputStream(fileName))){
			accounts = (List<Account>) ois.readObject();
			log.info("End of restore from:" + fileName);
		} catch (Exception e) {
			log.error(e.getMessage());
			throw new RuntimeException(e.toString());
		}

		return accounts;
	}

	@Override
	public void setAccounts(List<Account> accounts) {
		log.info("Begin of save in:" + fileName);
		List<Account> accForSave = new ArrayList<Account>(accounts);
		try(ObjectOutputStream ous = new ObjectOutputStream(new FileOutputStream(fileName))){
			ous.writeObject(accForSave);
			log.info("End of save in:" + fileName);
		} catch (Exception e) {
			log.error(e.getMessage());
			throw new RuntimeException(e.toString());
		} 

	}

}
