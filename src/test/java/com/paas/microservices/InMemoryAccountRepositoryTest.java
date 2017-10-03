package com.paas.microservices;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

public class InMemoryAccountRepositoryTest {
	private InMemoryAccountRepository repo;

	@Before
	public void setup() {
		repo = new InMemoryAccountRepository();
	}

	@Test
	public void accountsAreCreatable() {
		assertThat(repo.getAccountsSnapshot().size()).isEqualTo(0);
		AccountCreateRequestEvent requestEvent = new AccountCreateRequestEvent(UUID.randomUUID(), 0d);
		Integer createdId = repo.create(requestEvent);
		assertThat(createdId).isEqualTo(1);
		assertThat(repo.getAccountsSnapshot().size()).isEqualTo(1);
	}

	@Test
	public void writtenAccountsAreReadable() {
		Account account = new Account(123, 123.12d);
		Account returned = repo.save(new AccountUpdateRequestEvent(UUID.randomUUID(), account));
		assertThat(returned).isEqualTo(account);

		assertThat(repo.getAccountsSnapshot().size()).isEqualTo(1);

		Account actual = repo.load(123);
		assertThat(actual).isEqualTo(account);
	}
}
