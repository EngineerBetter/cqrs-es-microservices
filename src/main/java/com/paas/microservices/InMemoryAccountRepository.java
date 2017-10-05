package com.paas.microservices;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

public class InMemoryAccountRepository implements AccountRepository {
	private final EventStore eventStore;
	private final EventBus eventBus;

	public InMemoryAccountRepository(EventStore eventStore, EventBus eventBus) {
		this.eventStore = eventStore;
		this.eventBus = eventBus;
		eventBus.register(this);
	}

	@Override
	@Subscribe
	public void create(AccountCreateRequestEvent requestEvent) {
		Map<UUID, Account> snapshot = getAccountsSnapshot();

		if(eventStore.isNewEvent(requestEvent)) {
			eventStore.add(requestEvent);

			UUID newId = apply(requestEvent, snapshot);

			AccountCreatedEvent createdEvent = new AccountCreatedEvent(requestEvent.eventId, newId);
			eventStore.add(createdEvent);
			eventBus.post(createdEvent);
		} else {
			for(Event e: eventStore.getEvents()) {
				if(e instanceof AccountCreatedEvent) {
					AccountCreatedEvent ace = (AccountCreatedEvent) e;
					if(ace.parentEventId.equals(requestEvent.eventId)) {
						// Do nothing? Will be handled by EventBus?
					}
				}
			}

			throw new RuntimeException("Encountered duplicate create request, but could not find resulting created event");
		}
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
		if(eventStore.isNewEvent(requestEvent)) {
			eventStore.add(requestEvent);
			apply(requestEvent, getAccountsSnapshot());
			return requestEvent.account;
		} else {
			return getAccountsSnapshot().get(requestEvent.account.accountNumber);
		}
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
		Map<UUID, Account> accounts = new HashMap<>();

		for(Event e : eventStore.getEvents()) {
			if(e instanceof AccountCreateRequestEvent) {
				apply((AccountCreateRequestEvent) e, accounts);
			}

			if(e instanceof AccountUpdateRequestEvent) {
				apply((AccountUpdateRequestEvent) e, accounts);
			}
		}

		return accounts;
	}

	public void importEvents(Set<Event> other) {
		eventStore.importEvents(other);
	}

	public Set<Event> getEvents() {
		return eventStore.getEvents();
	}
}
