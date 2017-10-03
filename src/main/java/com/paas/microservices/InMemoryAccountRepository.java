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
		if(isNewEvent(requestEvent)) {
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
	public Account save(AccountUpdateRequestEvent requestEvent) {
		if(isNewEvent(requestEvent)) {
			events.put(requestEvent.transactionId, requestEvent);
			accounts.put(requestEvent.account.accountNumber, requestEvent.account);
			return requestEvent.account;
		} else {
			return accounts.get(requestEvent.account.accountNumber);
		}
	}

	@Override
	public Account load(Integer accountNumber) {
		return accounts.get(accountNumber);
	}

	@Override
	public Integer getTotalNumberOfAccounts() {
		return accounts.size();
	}

	private boolean isNewEvent(Event event) {
		return ! events.containsKey(event.getTransactionId());
	}
}
