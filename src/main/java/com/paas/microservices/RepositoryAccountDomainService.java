package com.paas.microservices;

import java.util.UUID;

public class RepositoryAccountDomainService implements AccountDomainService {
	private AccountRepository repo;

	public RepositoryAccountDomainService(AccountRepository repo) {
		this.repo = repo;
	}

	@Override
	public Account createAccount(UUID transactionId) {
		double startingBalance = 0d;
		Integer createdId = repo.create(new AccountCreateRequestEvent(transactionId, startingBalance));
		return new Account(createdId, startingBalance);
	}

	@Override
	public double getBalance(Integer accountNumber) {
		Account account = repo.load(accountNumber);
		return account.balance;
	}

}
