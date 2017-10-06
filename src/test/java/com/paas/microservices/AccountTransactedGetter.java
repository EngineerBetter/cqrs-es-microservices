package com.paas.microservices;

import com.google.common.eventbus.Subscribe;
import com.paas.microservices.domain.AccountTransactedDomainEvent;

public class AccountTransactedGetter {
	private AccountTransactedDomainEvent event;

	@Subscribe
	public void handler(AccountTransactedDomainEvent event) {
		this.event = event;
	}

	public AccountTransactedDomainEvent getEvent() {
		return event;
	}
}