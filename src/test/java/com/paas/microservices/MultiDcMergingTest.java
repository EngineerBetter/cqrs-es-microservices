package com.paas.microservices;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.paas.microservices.data.InMemoryAccountRepository;
import com.paas.microservices.domain.AccountCreateRequestDomainEvent;
import com.paas.microservices.domain.AccountCreditRequestDomainEvent;
import com.paas.microservices.domain.RepositoryAccountDomainService;

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
		AccountGetter thingy = new AccountGetter();
		leftEventBus.register(thingy);
		rightEventBus.register(thingy);
		leftEventBus.post(new AccountCreateRequestDomainEvent(UUID.randomUUID()));
		Account leftAccount = thingy.getAccount();
		rightEventBus.post(new AccountCreateRequestDomainEvent(UUID.randomUUID()));
		Account rightAccount = thingy.getAccount();

		leftEventBus.post(new AccountCreditRequestDomainEvent(UUID.randomUUID(), leftAccount.accountNumber, 100d));
		rightEventBus.post(new AccountCreditRequestDomainEvent(UUID.randomUUID(), rightAccount.accountNumber, 60d));

		Set<Event> merged = new LinkedHashSet<>();
		merged.addAll(leftEventBus.getEvents());
		merged.addAll(rightEventBus.getEvents());

		leftEventBus.importEvents(merged);
		double balance = leftService.getBalance(leftAccount.accountNumber);
		assertThat(balance).isEqualTo(100d);

		rightEventBus.importEvents(merged);
		balance = rightService.getBalance(rightAccount.accountNumber);
		assertThat(balance).isEqualTo(60d);
	}

	@Ignore
	@Test
	public void debitsThatResultInOverdrawnAccountsAreRejectedOnMerge() {
		UUID accountNumber = UUID.randomUUID();
		AccountCreateRequestDomainEvent createReqEvent = new AccountCreateRequestDomainEvent(accountNumber);
		leftEventBus.post(createReqEvent);
		leftEventBus.post(new AccountCreditRequestDomainEvent(UUID.randomUUID(), accountNumber, 100d));
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
