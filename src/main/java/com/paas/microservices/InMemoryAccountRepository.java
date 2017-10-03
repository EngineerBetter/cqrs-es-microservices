package com.paas.microservices;

import java.util.HashMap;
import java.util.Map;

public class InMemoryAccountRepository implements AccountRepository {
	private final Map<Integer, Account> accounts;

	public InMemoryAccountRepository() {
		accounts = new HashMap<>();
	}

	@Override
	public Integer create(AccountCreateRequestEvent event) {
		Integer newId = accounts.size() + 1;
		Account newAccount = new Account(newId, event.startingBalance);
		accounts.put(newId, newAccount);
		return newId;
	}

	@Override
	public Account save(AccountUpdateRequestEvent event) {
		accounts.put(event.account.accountNumber, event.account);
		return event.account;
	}

	@Override
	public Account load(Integer accountNumber) {
		return accounts.get(accountNumber);
	}

}
