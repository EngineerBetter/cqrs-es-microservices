package com.paas.microservices;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.paas.microservices.TransactionRow.TransactionType;

public class RepositoryAccountDomainService implements AccountDomainService {
	private AccountRepository repo;
	Map<UUID, TransactionRow> transactions;
	private final StoringEventBus eventBus;

	public RepositoryAccountDomainService(AccountRepository repo, StoringEventBus eventBus) {
		this.repo = repo;
		this.eventBus = eventBus;
		eventBus.register(this);
	}

	@Override
	public Account createAccount(UUID eventId) {
		double startingBalance = 0d;
		eventBus.post(new AccountCreateRequestEvent(eventId, startingBalance));
		return new Account(eventId, startingBalance);
	}

	@Override
	public void creditAccount(UUID eventId, UUID accountNumber, double amount) {
		double previousBalance = getBalance(accountNumber);
		double newBalance = previousBalance + amount;
		Account account = new Account(accountNumber, newBalance);
		AccountUpdateRequestEvent updateRequest = new AccountUpdateRequestEvent(eventId, account);
		eventBus.post(updateRequest);
		eventBus.post(new AccountCreditedEvent(eventId, accountNumber, amount, newBalance));
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
			eventBus.post(updateRequest);
			eventBus.post(new AccountDebitedEvent(debitEventId, accountNumber, amountToBeDebited, newBalance));
		} else {
			//TODO: create a failed debit event
			throw new RuntimeException("You are trying to debit more than your account balance currently has");
		}
	}

	@Override
	public TransactionHistory getTransactionHistory(UUID accountNumber) {
		List<TransactionRow> rows = new ArrayList<>();

		for(Event e : eventBus.getEvents()) {
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

	public void importEvents(Set<Event> other) {
		eventBus.importEvents(other);
	}

	public Set<Event> getEvents() {
		return eventBus.getEvents();
	}

}
