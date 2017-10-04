package com.paas.microservices;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class InMemoryAccountRepository implements AccountRepository {
	private final Set<Event> events;

	public InMemoryAccountRepository() {
		events = new LinkedHashSet<>();
	}

	@Override
	public UUID create(AccountCreateRequestEvent requestEvent) {
		Map<UUID, Account> snapshot = getAccountsSnapshot();

		if(isNewEvent(requestEvent)) {
			events.add(requestEvent);

			UUID newId = apply(requestEvent, snapshot);

			AccountCreatedEvent createdEvent = new AccountCreatedEvent(requestEvent.transactionId, newId);
			events.add(createdEvent);
			return newId;
		} else {
			for(Event e: events) {
				if(e instanceof AccountCreatedEvent) {
					AccountCreatedEvent ace = (AccountCreatedEvent) e;
					if(ace.parentTransactionId.equals(requestEvent.transactionId)) {
						return ace.createdId;
					}
				}
			}

			throw new RuntimeException("Encountered duplicate create request, but could not find resulting created event");
		}
	}

	private UUID apply(AccountCreateRequestEvent requestEvent, Map<UUID, Account> accounts) {
		UUID newId = requestEvent.transactionId;
		Account newAccount = new Account(requestEvent.transactionId, requestEvent.startingBalance);
		accounts.put(newId, newAccount);
		return newId;
	}

	@Override
	public Account save(AccountUpdateRequestEvent requestEvent) {
		if(isNewEvent(requestEvent)) {
			events.add(requestEvent);
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

	private boolean isNewEvent(Event event) {
		return ! events.contains(event);
	}

	protected Map<UUID, Account> getAccountsSnapshot() {
		Map<UUID, Account> accounts = new HashMap<>();

		for(Event e : events) {
			if(e instanceof AccountCreateRequestEvent) {
				apply((AccountCreateRequestEvent) e, accounts);
			}

			if(e instanceof AccountUpdateRequestEvent) {
				apply((AccountUpdateRequestEvent) e, accounts);
			}
		}

		return accounts;
	}

	protected Set<Event> getEvents() {
		return Collections.unmodifiableSet(events);
	}

	protected void importEvents(Set<Event> importedEvents) {
		events.addAll(importedEvents);
	}
}
