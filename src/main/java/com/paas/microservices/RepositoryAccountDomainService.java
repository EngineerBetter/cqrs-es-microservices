package com.paas.microservices;

import java.util.Map;
import java.util.UUID;

public class RepositoryAccountDomainService implements AccountDomainService {
	private AccountRepository repo;
	public enum EventType { CREDIT, DEBIT};
	EventType type;
	Map<UUID, TransactionRow> transactions;

	public RepositoryAccountDomainService(AccountRepository repo) {
		this.repo = repo;
	}

	@Override
	public Account createAccount(UUID transactionId) {
		double startingBalance = 0d;
		UUID createdId = repo.create(new AccountCreateRequestEvent(transactionId, startingBalance));
		return new Account(createdId, startingBalance);
	}

	@Override
	public void creditAccount(UUID transactionId, UUID accountNumber, double amount) {
		this.type = EventType.CREDIT;
		double previousBalance = getBalance(accountNumber);
		double newBalance = previousBalance + amount;
		Account account = new Account(accountNumber, newBalance);
		AccountUpdateRequestEvent updateRequest = new AccountUpdateRequestEvent(transactionId, account);
		repo.save(updateRequest);
	}

	@Override
	public double getBalance(UUID accountNumber) {
		Account account = repo.load(accountNumber);
		return account.balance;
	}

	@Override
	public void debitAccount(UUID debitTxId, UUID accountNumber, double amountToBeDebited) {
		this.type = EventType.DEBIT;
		double previousBalance = getBalance(accountNumber);
		double newBalance = previousBalance - amountToBeDebited;

		if(newBalance >= 0d) {
			Account account = new Account(accountNumber, newBalance);
			AccountUpdateRequestEvent updateRequest = new AccountUpdateRequestEvent(debitTxId, account);
			repo.save(updateRequest);
		} else {
			//TODO: create a failed debit event
			throw new RuntimeException("You are trying to debit more than your account balance currently has");
		}
	}

	@Override
	public TransactionHistory getTransactionHistory(UUID accountNumber) {
		return repo.getTransactionHistory(accountNumber);
	}

}
