package com.paas.microservices;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class MultiDcMergingTest {
	private StoringEventBus leftEventBus, rightEventBus;
	private RepositoryAccountDomainService leftService, rightService;
	private InMemoryAccountRepository leftRepo, rightRepo;

	@Before
	public void setup() {
		leftEventBus = new StoringEventBus();
		rightEventBus = new StoringEventBus();
		leftRepo = new InMemoryAccountRepository(leftEventBus);
		rightRepo = new InMemoryAccountRepository(rightEventBus);
		leftService = new RepositoryAccountDomainService(leftRepo, leftEventBus);
		rightService = new RepositoryAccountDomainService(rightRepo, rightEventBus);
	}

	@Test
	public void accountNumbersDoNotClash() {
		Account leftAccount = leftService.createAccount(UUID.randomUUID());
		Account rightAccount = rightService.createAccount(UUID.randomUUID());

		leftService.creditAccount(UUID.randomUUID(), leftAccount.accountNumber, 100d);
		rightService.creditAccount(UUID.randomUUID(), rightAccount.accountNumber, 60d);

		Set<Event> merged = new LinkedHashSet<>();
		merged.addAll(leftEventBus.getEvents());
		merged.addAll(rightEventBus.getEvents());

		leftEventBus.importEvents(merged);
		assertThat(leftEventBus.getEvents().size()).isEqualTo(8); //More messages created by domain service
		double balance = leftService.getBalance(leftAccount.accountNumber);
		assertThat(balance).isEqualTo(100d);
	}

	@Ignore
	@Test
	public void debitsThatResultInOverdrawnAccountsAreRejectedOnMerge() {
		UUID accountNumber = UUID.randomUUID();
		leftService.createAccount(accountNumber);
		leftService.creditAccount(UUID.randomUUID(), accountNumber, 100d);
		merge();
		assertThat(leftService.getBalance(accountNumber)).isEqualTo(100d);
		assertThat(rightService.getBalance(accountNumber)).isEqualTo(100d);

		leftService.debitAccount(UUID.randomUUID(), accountNumber, 70d);
		assertThat(leftService.getBalance(accountNumber)).isEqualTo(30d);

		rightService.debitAccount(UUID.randomUUID(), accountNumber, 60d);
		assertThat(rightService.getBalance(accountNumber)).isEqualTo(40d);

		merge();
		assertThat(rightService.getBalance(accountNumber)).isEqualTo(30d);
		assertThat(leftService.getBalance(accountNumber)).isEqualTo(30d);
	}

	private void merge() {
		Set<Event> merged = new LinkedHashSet<>();
		merged.addAll(leftEventBus.getEvents());
		merged.addAll(rightEventBus.getEvents());

		leftEventBus.importEvents(merged);
		rightEventBus.importEvents(merged);
	}
}
