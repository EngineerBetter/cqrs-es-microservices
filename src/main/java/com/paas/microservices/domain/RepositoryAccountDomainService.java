package com.paas.microservices.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.google.common.eventbus.Subscribe;
import com.paas.microservices.Account;
import com.paas.microservices.Event;
import com.paas.microservices.StoringEventBus;
import com.paas.microservices.data.AccountBalanceSetRequestDataEvent;
import com.paas.microservices.data.AccountCreateRequestDataEvent;
import com.paas.microservices.data.AccountCreatedDataEvent;
import com.paas.microservices.data.AccountRepository;
import com.paas.microservices.data.AccountUpdatedDataEvent;

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
		AccountCreateRequestDataEvent dataEvent = new AccountCreateRequestDataEvent(event.eventId, startingBalance, event);
		eventBus.post(dataEvent);
		Account account = new Account(event.eventId, startingBalance);
		pendingResponses.put(dataEvent, new AccountCreatedDomainEvent(event.eventId, account, event));
	}

	Map<Event, Event> pendingResponses = new HashMap<>();

	@Subscribe
	public void handle(AccountCreatedDataEvent event) {
		if(pendingResponses.containsKey(event.getCause())) {
			AccountCreatedDomainEvent old = (AccountCreatedDomainEvent) pendingResponses.get(event.getCause());
			eventBus.post(new AccountCreatedDomainEvent(event.parentEventId, old.account, event));
		} else {
			throw new RuntimeException("Could not find response for event "+event);
		}
	}

	@Override
	@Subscribe
	public void creditAccount(AccountTransactionRequestDomainEvent event) {
		double previousBalance = getBalance(event.accountNumber);
		double newBalance = previousBalance;

		if(event.type == TransactionType.CREDIT) {
			newBalance = previousBalance + event.amount;
		} else {
			newBalance = previousBalance - event.amount;

			if(newBalance < 0) {
				throw new RuntimeException("You are trying to debit more than your account balance currently has");
			}
		}

		Account account = new Account(event.accountNumber, newBalance);
		AccountBalanceSetRequestDataEvent updateRequest = new AccountBalanceSetRequestDataEvent(event.eventId, account, event);
		eventBus.post(updateRequest);
		pendingResponses.put(updateRequest, new AccountTransactedDomainEvent(event.eventId, event.accountNumber, event.amount, newBalance, event));

		if(event.type == TransactionType.CREDIT) {
			addToHistory(account, TransactionType.CREDIT, event.amount);
		} else {
			addToHistory(account, TransactionType.DEBIT, event.amount);
		}
	}

	@Subscribe
	public void handle(AccountUpdatedDataEvent event) {
		if(pendingResponses.containsKey(event.getCause())) {
			AccountTransactedDomainEvent old = (AccountTransactedDomainEvent) pendingResponses.get(event.getCause());
			eventBus.post(new AccountTransactedDomainEvent(old.eventId, old.accountNumber, old.amount, old.resultingBalance, event));
		} else {
			throw new RuntimeException("Could not find response for event "+event);
		}
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
	public TransactionHistory getTransactionHistory(UUID accountNumber) {
		return histories.get(accountNumber);
	}

	private TransactionHistory getOrNewTransactionHistory(UUID accountNumber) {
		return histories.getOrDefault(accountNumber, new TransactionHistory(new ArrayList<>()));
	}
}
