package com.paas.microservices;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class InMemoryAccountRepository implements AccountRepository {
	private final Map<Integer, Account> accounts;
	private final Map<UUID, Event> events;

	public InMemoryAccountRepository() {
		accounts = new HashMap<>();
		events = new HashMap<>();
	}

	@Override
	public Integer create(AccountCreateRequestEvent requestEvent) {
		if(eventHasNotBeenSeenBefore(requestEvent)) {
			events.put(requestEvent.getTransactionId(), requestEvent);
			Integer newId = accounts.size() + 1;
			Account newAccount = new Account(newId, requestEvent.startingBalance);
			accounts.put(newId, newAccount);
			AccountCreatedEvent createdEvent = new AccountCreatedEvent(UUID.randomUUID(), requestEvent.transactionId, newId);
			events.put(createdEvent.transactionId, createdEvent);
			return newId;
		} else {
			for(Event e: events.values()) {
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

	@Override
	public Account save(AccountUpdateRequestEvent event) {
		accounts.put(event.account.accountNumber, event.account);
		return event.account;
	}

	@Override
	public Account load(Integer accountNumber) {
		return accounts.get(accountNumber);
	}

	@Override
	public Integer getTotalNumberOfAccounts() {
		return accounts.size();
	}

	private boolean eventHasNotBeenSeenBefore(Event event) {
		return ! events.containsKey(event.getTransactionId());
	}
}
