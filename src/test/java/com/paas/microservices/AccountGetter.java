package com.paas.microservices;

import com.google.common.eventbus.Subscribe;
import com.paas.microservices.domain.AccountCreatedDomainEvent;

public class AccountGetter {
	private Account account;
	private AccountCreatedDomainEvent event;

	@Subscribe
	public void handler(AccountCreatedDomainEvent event) {
		account = event.account;
		this.event = event;
	}

	public Account getAccount() {
		return account;
	}

	public AccountCreatedDomainEvent getEvent() {
		return event;
	}
}