package com.paas.microservices;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;

public class AccountDomainServiceTest {
	private AccountDomainService accountService;

	@Before
	public void setup() {
		accountService = new AccountDomainService();
	}

	@Test
	public void accountBalanceIsGettable() {
		Double balance = accountService.getBalance(Integer accountNumber);
		assertThat(balance).isNotNull();
	}
}
