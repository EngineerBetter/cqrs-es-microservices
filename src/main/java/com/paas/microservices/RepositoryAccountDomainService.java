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
	public void creditAccount(UUID transactionId, Integer accountNumber, double amount) {
		double previousBalance = getBalance(accountNumber);
		double newBalance = previousBalance + amount;
		Account account = new Account(accountNumber, newBalance);
		AccountUpdateRequestEvent updateRequest = new AccountUpdateRequestEvent(transactionId, account);
		repo.save(updateRequest);
	}

	@Override
	public double getBalance(Integer accountNumber) {
		Account account = repo.load(accountNumber);
		return account.balance;
	}

}
