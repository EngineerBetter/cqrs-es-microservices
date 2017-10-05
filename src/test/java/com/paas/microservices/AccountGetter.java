package com.paas.microservices;

import com.google.common.eventbus.Subscribe;
import com.paas.microservices.domain.AccountCreatedDomainEvent;

public class AccountGetter {
	private Account account;

	@Subscribe
	public void handler(AccountCreatedDomainEvent event) {
		account = event.account;
	}

	public Account getAccount() {
		return account;
	}
}