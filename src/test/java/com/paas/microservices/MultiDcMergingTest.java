package com.paas.microservices;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

public class MultiDcMergingTest {
	private AccountDomainService leftService, rightService;
	private InMemoryAccountRepository leftRepo, rightRepo;

	@Before
	public void setup() {
		leftRepo = new InMemoryAccountRepository();
		rightRepo = new InMemoryAccountRepository();
		leftService = new RepositoryAccountDomainService(leftRepo);
		rightService = new RepositoryAccountDomainService(rightRepo);
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
		assertThat(leftRepo.getEvents().size()).isEqualTo(6);
		double balance = leftService.getBalance(leftAccount.accountNumber);
		assertThat(balance).isEqualTo(100d);
	}
}
