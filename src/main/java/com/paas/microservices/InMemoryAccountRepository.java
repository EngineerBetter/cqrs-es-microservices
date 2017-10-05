package com.paas.microservices;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.google.common.eventbus.Subscribe;

public class InMemoryAccountRepository implements AccountRepository {
	private final StoringEventBus eventBus;
	private final Map<UUID, Account> accounts;

	public InMemoryAccountRepository(StoringEventBus eventBus) {
		this.eventBus = eventBus;
		eventBus.register(this);
		this.accounts = new HashMap<>();
	}

	@Override
	@Subscribe
	public void create(AccountCreateRequestEvent requestEvent) {
		Map<UUID, Account> snapshot = getAccountsSnapshot();

		UUID newId = apply(requestEvent, snapshot);

		AccountCreatedEvent createdEvent = new AccountCreatedEvent(requestEvent.eventId, newId);
		eventBus.post(createdEvent);
	}

	private UUID apply(AccountCreateRequestEvent requestEvent, Map<UUID, Account> accounts) {
		UUID newId = requestEvent.eventId;
		Account newAccount = new Account(requestEvent.eventId, requestEvent.startingBalance);
		accounts.put(newId, newAccount);
		return newId;
	}

	@Override
	@Subscribe
	public Account save(AccountUpdateRequestEvent requestEvent) {
		apply(requestEvent, getAccountsSnapshot());
		return requestEvent.account;
	}

	private void apply(AccountUpdateRequestEvent requestEvent, Map<UUID, Account> accounts) {
		accounts.put(requestEvent.account.accountNumber, requestEvent.account);
	}

	@Override
	public Account load(UUID accountNumber) {
		Map<UUID, Account> snapshot = getAccountsSnapshot();

		if(! snapshot.containsKey(accountNumber)) {
			throw new RuntimeException("AccountNumber ["+accountNumber+"] did not exist");
		}

		return getAccountsSnapshot().get(accountNumber);
	}

	@Override
	public Integer getTotalNumberOfAccounts() {
		return getAccountsSnapshot().size();
	}

	protected Map<UUID, Account> getAccountsSnapshot() {
		return accounts;
	}


	public void importEvents(Set<Event> other) {
		eventBus.importEvents(other);
	}

	public Set<Event> getEvents() {
		return eventBus.getEvents();
	}
}
