package com.paas.microservices;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.google.common.eventbus.EventBus;

public class MultiDcMergingTest {
	private EventStore leftEventStore, rightEventStore;
	private RepositoryAccountDomainService leftService, rightService;
	private InMemoryAccountRepository leftRepo, rightRepo;

	@Before
	public void setup() {
		leftEventStore = new EventStore();
		rightEventStore = new EventStore();
		leftRepo = new InMemoryAccountRepository(leftEventStore);
		rightRepo = new InMemoryAccountRepository(rightEventStore);
		leftService = new RepositoryAccountDomainService(leftRepo, leftEventStore, new EventBus());
		rightService = new RepositoryAccountDomainService(rightRepo, rightEventStore, new EventBus());
	}

	@Test
	public void accountNumbersDoNotClash() {
		Account leftAccount = leftService.createAccount(UUID.randomUUID());
		Account rightAccount = rightService.createAccount(UUID.randomUUID());

		leftService.creditAccount(UUID.randomUUID(), leftAccount.accountNumber, 100d);
		rightService.creditAccount(UUID.randomUUID(), rightAccount.accountNumber, 60d);

		Set<Event> merged = new LinkedHashSet<>();
		merged.addAll(leftRepo.getEvents());
		merged.addAll(rightRepo.getEvents());

		leftRepo.importEvents(merged);
		assertThat(leftRepo.getEvents().size()).isEqualTo(8); //More messages created by domain service
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
		mergeServices();
		mergeRepos();
	}

	private void mergeServices() {
		Set<Event> merged = new LinkedHashSet<>();
		merged.addAll(leftService.getEvents());
		merged.addAll(rightService.getEvents());

		leftService.importEvents(merged);
		rightService.importEvents(merged);
	}

	private void mergeRepos() {
		Set<Event> merged = new LinkedHashSet<>();
		merged.addAll(leftRepo.getEvents());
		merged.addAll(rightRepo.getEvents());

		leftRepo.importEvents(merged);
		rightRepo.importEvents(merged);
	}
}
