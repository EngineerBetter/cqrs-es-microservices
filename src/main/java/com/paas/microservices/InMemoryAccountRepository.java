package com.paas.microservices;

import java.util.HashMap;
import java.util.Map;

public class InMemoryAccountRepository implements AccountRepository {
	private final Map<Integer, Account> accounts;

	public InMemoryAccountRepository() {
		accounts = new HashMap<>();
	}

	@Override
	public Account save(Account account) {
		accounts.put(account.accountNumber, account);
		return account;
	}

	@Override
	public Account load(Integer accountNumber) {
		return accounts.get(accountNumber);
	}

}
