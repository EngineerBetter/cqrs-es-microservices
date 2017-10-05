package com.paas.microservices.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.google.common.eventbus.Subscribe;
import com.paas.microservices.Account;
import com.paas.microservices.StoringEventBus;
import com.paas.microservices.data.AccountBalanceSetRequestDataEvent;
import com.paas.microservices.data.AccountCreateRequestDataEvent;
import com.paas.microservices.data.AccountRepository;
import com.paas.microservices.domain.TransactionRow.TransactionType;

public class RepositoryAccountDomainService implements AccountDomainService {
	private AccountRepository repo;
	private final StoringEventBus eventBus;
	private Map<UUID, TransactionHistory> histories;

	public RepositoryAccountDomainService(AccountRepository repo, StoringEventBus eventBus) {
		this.repo = repo;
		this.eventBus = eventBus;
		eventBus.register(this);
		this.histories = new HashMap<>();
	}

	@Override
	@Subscribe
	public void createAccount(AccountCreateRequestDomainEvent event) {
		double startingBalance = 0d;
		eventBus.post(new AccountCreateRequestDataEvent(event.eventId, startingBalance));
		Account account = new Account(event.eventId, startingBalance);
		eventBus.post(new AccountCreatedDomainEvent(event.eventId, account));
	}

	@Override
	public void creditAccount(UUID eventId, UUID accountNumber, double amount) {
		double previousBalance = getBalance(accountNumber);
		double newBalance = previousBalance + amount;
		Account account = new Account(accountNumber, newBalance);
		AccountBalanceSetRequestDataEvent updateRequest = new AccountBalanceSetRequestDataEvent(eventId, account);
		eventBus.post(updateRequest);
		eventBus.post(new AccountCreditedDomainEvent(eventId, accountNumber, amount, newBalance));

		addToHistory(account, TransactionType.CREDIT, amount);
	}

	private void addToHistory(Account account, TransactionType type, double amount) {
		TransactionRow newTransaction = new TransactionRow(type, amount, account.balance);
		TransactionHistory newHistory = getOrNewTransactionHistory(account.accountNumber).buildNewHistoryWith(newTransaction);
		histories.put(account.accountNumber, newHistory);
	}

	@Override
	public double getBalance(UUID accountNumber) {
		Account account = repo.load(accountNumber);
		return account.balance;
	}

	@Override
	public void debitAccount(UUID debitEventId, UUID accountNumber, double amount) {
		double previousBalance = getBalance(accountNumber);
		double newBalance = previousBalance - amount;

		if(newBalance >= 0d) {
			Account account = new Account(accountNumber, newBalance);
			AccountBalanceSetRequestDataEvent updateRequest = new AccountBalanceSetRequestDataEvent(debitEventId, account);
			eventBus.post(updateRequest);
			eventBus.post(new AccountDebitedDomainEvent(debitEventId, accountNumber, amount, newBalance));

			addToHistory(account, TransactionType.DEBIT, amount);
		} else {
			//TODO: create a failed debit event
			throw new RuntimeException("You are trying to debit more than your account balance currently has");
		}
	}

	@Override
	public TransactionHistory getTransactionHistory(UUID accountNumber) {
		return histories.get(accountNumber);
	}

	private TransactionHistory getOrNewTransactionHistory(UUID accountNumber) {
		return histories.getOrDefault(accountNumber, new TransactionHistory(new ArrayList<>()));
	}
}
