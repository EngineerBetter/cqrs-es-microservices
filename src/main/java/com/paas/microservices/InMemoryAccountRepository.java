package com.paas.microservices;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class InMemoryAccountRepository implements AccountRepository {
	private final Set<Event> events;

	public InMemoryAccountRepository() {
		events = new LinkedHashSet<>();
	}

	@Override
	public Integer create(AccountCreateRequestEvent requestEvent) {
		Map<Integer, Account> snapshot = getAccountsSnapshot();

		if(isNewEvent(requestEvent)) {
			events.add(requestEvent);

			Integer newId = apply(requestEvent, snapshot);

			AccountCreatedEvent createdEvent = new AccountCreatedEvent(UUID.randomUUID(), requestEvent.transactionId, newId);
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

	private Integer apply(AccountCreateRequestEvent requestEvent, Map<Integer, Account> accounts) {
		Integer newId = accounts.size() + 1;
		Account newAccount = new Account(newId, requestEvent.startingBalance);
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

	private void apply(AccountUpdateRequestEvent requestEvent, Map<Integer, Account> accounts) {
		accounts.put(requestEvent.account.accountNumber, requestEvent.account);
	}

	@Override
	public Account load(Integer accountNumber) {
		return getAccountsSnapshot().get(accountNumber);
	}

	@Override
	public Integer getTotalNumberOfAccounts() {
		return getAccountsSnapshot().size();
	}

	private boolean isNewEvent(Event event) {
		return ! events.contains(event);
	}

	protected Map<Integer, Account> getAccountsSnapshot() {
		Map<Integer, Account> accounts = new HashMap<>();

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
