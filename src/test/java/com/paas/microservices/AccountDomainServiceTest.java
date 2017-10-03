package com.paas.microservices;

import static org.assertj.core.api.Assertions.assertThat;

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
		Account account = accountService.createAccount();
		assertThat(account.balance).isEqualTo(0d);
	}
}
