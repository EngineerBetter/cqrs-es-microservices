package com.paas.microservices;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

public class AccountDomainServiceTest {
	private AccountDomainService accountService;

	@Before
	public void setup() {
		accountService = new RepositoryAccountDomainService(new InMemoryAccountRepository());
	}

	@Test
	public void newAccountsHaveAZeroBalance() {
		UUID transactionId = UUID.randomUUID();
		Account account = accountService.createAccount(transactionId);
		assertThat(account.balance).isEqualTo(0d);
	}
}
