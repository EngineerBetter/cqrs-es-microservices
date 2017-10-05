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
		eventBus.post(new AccountCreateRequestDataEvent(event.eventId, startingBalance, event));
		Account account = new Account(event.eventId, startingBalance);
		eventBus.post(new AccountCreatedDomainEvent(event.eventId, account));
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
		eventBus.post(new AccountTransactedDomainEvent(event.eventId, event.accountNumber, event.amount, newBalance));

		if(event.type == TransactionType.CREDIT) {
			addToHistory(account, TransactionType.CREDIT, event.amount);
		} else {
			addToHistory(account, TransactionType.DEBIT, event.amount);
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
