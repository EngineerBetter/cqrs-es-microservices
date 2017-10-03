package com.paas.microservices;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;

public class InMemoryAccountRepositoryTest {
	private AccountRepository repo;

	@Before
	public void setup() {
		repo = new InMemoryAccountRepository();
	}

	@Test
	public void writtenAccountsAreReadable() {
		Account account = new Account(123, 123.12d);
		Account returned = repo.save(new AccountUpdateRequestEvent(account));
		assertThat(returned).isEqualTo(account);
		Account actual = repo.load(123);
		assertThat(actual).isEqualTo(account);
	}
}
