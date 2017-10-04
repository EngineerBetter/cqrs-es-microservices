package com.paas.microservices;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.paas.microservices.TransactionRow.TransactionType;

public class RepositoryAccountDomainService implements AccountDomainService {
	private AccountRepository repo;
	Map<UUID, TransactionRow> transactions;
	private final EventStore eventStore;

	public RepositoryAccountDomainService(AccountRepository repo) {
		this.repo = repo;
		this.eventStore = new EventStore();
	}

	@Override
	public Account createAccount(UUID transactionId) {
		double startingBalance = 0d;
		UUID createdId = repo.create(new AccountCreateRequestEvent(transactionId, startingBalance));
		return new Account(createdId, startingBalance);
	}

	@Override
	public void creditAccount(UUID eventId, UUID accountNumber, double amount) {
		double previousBalance = getBalance(accountNumber);
		double newBalance = previousBalance + amount;
		Account account = new Account(accountNumber, newBalance);
		AccountUpdateRequestEvent updateRequest = new AccountUpdateRequestEvent(eventId, account);
		repo.save(updateRequest);
		eventStore.add(new AccountCreditedEvent(eventId, accountNumber, amount, newBalance));
	}

	@Override
	public double getBalance(UUID accountNumber) {
		Account account = repo.load(accountNumber);
		return account.balance;
	}

	@Override
	public void debitAccount(UUID debitEventId, UUID accountNumber, double amountToBeDebited) {
		double previousBalance = getBalance(accountNumber);
		double newBalance = previousBalance - amountToBeDebited;

		if(newBalance >= 0d) {
			Account account = new Account(accountNumber, newBalance);
			AccountUpdateRequestEvent updateRequest = new AccountUpdateRequestEvent(debitEventId, account);
			repo.save(updateRequest);
			eventStore.add(new AccountDebitedEvent(debitEventId, accountNumber, amountToBeDebited, newBalance));
		} else {
			//TODO: create a failed debit event
			throw new RuntimeException("You are trying to debit more than your account balance currently has");
		}
	}

	@Override
	public TransactionHistory getTransactionHistory(UUID accountNumber) {
		List<TransactionRow> rows = new ArrayList<>();

		for(Event e : eventStore.getEvents()) {
			if(e instanceof AccountCreditedEvent && accountNumber.equals(((AccountCreditedEvent) e).accountNumber)) {
				AccountCreditedEvent cae = (AccountCreditedEvent) e;
				rows.add(new TransactionRow(TransactionType.CREDIT,cae.amount, cae.resultingBalance));
			}

			if(e instanceof AccountDebitedEvent && accountNumber.equals(((AccountDebitedEvent) e).accountNumber)) {
				AccountDebitedEvent cae = (AccountDebitedEvent) e;
				rows.add(new TransactionRow(TransactionType.DEBIT,cae.amount, cae.resultingBalance));
			}
		}

		return new TransactionHistory(rows);
	}

}
