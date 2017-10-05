package com.paas.microservices;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.google.common.eventbus.Subscribe;

public class InMemoryAccountRepository implements AccountRepository, ResetHandler {
	private final StoringEventBus eventBus;
	private final Map<UUID, Account> accounts;

	public InMemoryAccountRepository(StoringEventBus eventBus) {
		this.eventBus = eventBus;
		eventBus.register(this);
		this.accounts = new HashMap<>();
	}

	@Override
	public void handle(ResetStateEvent event) {
		accounts.clear();
	}

	@Override
	@Subscribe
	public void create(AccountCreateRequestEvent requestEvent) {
		UUID newId = requestEvent.eventId;
		Account newAccount = new Account(requestEvent.eventId, requestEvent.startingBalance);
		accounts.put(newId, newAccount);

		AccountCreatedEvent createdEvent = new AccountCreatedEvent(requestEvent.eventId, newId);
		eventBus.post(createdEvent);
	}

	@Override
	@Subscribe
	public Account save(AccountUpdateRequestEvent requestEvent) {
		accounts.put(requestEvent.account.accountNumber, requestEvent.account);
		return requestEvent.account;
	}

	@Override
	public Account load(UUID accountNumber) {
		if(! accounts.containsKey(accountNumber)) {
			throw new RuntimeException("AccountNumber ["+accountNumber+"] did not exist");
		}

		return accounts.get(accountNumber);
	}

	@Override
	public Integer getTotalNumberOfAccounts() {
		return accounts.size();
	}
}
