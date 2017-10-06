package com.paas.microservices;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import com.paas.microservices.data.InMemoryAccountRepository;
import com.paas.microservices.domain.AccountCreateRequestDomainEvent;
import com.paas.microservices.domain.AccountCreatedDomainEvent;
import com.paas.microservices.domain.AccountTransactedDomainEvent;
import com.paas.microservices.domain.AccountTransactionRequestDomainEvent;
import com.paas.microservices.domain.RepositoryAccountDomainService;
import com.paas.microservices.domain.TransactionType;

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
		AccountCreatedDomainEvent leftCreatedEvent = thingy.getEvent();
		rightEventBus.post(new AccountCreateRequestDomainEvent(UUID.randomUUID()));
		Account rightAccount = thingy.getAccount();
		AccountCreatedDomainEvent rightCreatedEvent = thingy.getEvent();

		leftEventBus.post(new AccountTransactionRequestDomainEvent(UUID.randomUUID(), leftAccount.accountNumber, 100d, TransactionType.CREDIT, leftCreatedEvent));
		rightEventBus.post(new AccountTransactionRequestDomainEvent(UUID.randomUUID(), rightAccount.accountNumber, 60d, TransactionType.CREDIT, rightCreatedEvent));

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

	@Test
	public void debitsThatResultInOverdrawnAccountsAreRejectedOnMerge() {
		AccountGetter thingy = new AccountGetter();
		leftEventBus.register(thingy);
		rightEventBus.register(thingy);
		AccountTransactedGetter transactedGetter = new AccountTransactedGetter();
		leftEventBus.register(transactedGetter);
		rightEventBus.register(transactedGetter);

		UUID accountNumber = UUID.fromString("acc00000-0000-0000-0000-000000000000");
		AccountCreateRequestDomainEvent createReqEvent = new AccountCreateRequestDomainEvent(accountNumber);
		leftEventBus.post(createReqEvent);
		AccountCreatedDomainEvent leftCreatedEvent = thingy.getEvent();
		leftEventBus.post(new AccountTransactionRequestDomainEvent(UUID.fromString("00000000-0000-0000-0000-000000000100"), accountNumber, 100d, TransactionType.CREDIT, leftCreatedEvent));
		AccountTransactedDomainEvent leftTxEvent = transactedGetter.getEvent();
		merge();
		assertThat(leftService.getBalance(accountNumber)).isEqualTo(100d);
		assertThat(rightService.getBalance(accountNumber)).isEqualTo(100d);

		leftEventBus.post(new AccountTransactionRequestDomainEvent(UUID.fromString("00000000-0000-0000-0000-000000000070"), accountNumber, 70d, TransactionType.DEBIT, leftTxEvent));
		assertThat(leftService.getBalance(accountNumber)).isEqualTo(30d);

		rightEventBus.post(new AccountTransactionRequestDomainEvent(UUID.fromString("00000000-0000-0000-0000-000000000060"), accountNumber, 60d, TransactionType.DEBIT, leftTxEvent));
		assertThat(rightService.getBalance(accountNumber)).isEqualTo(40d);

		merge();
		assertThat(rightService.getBalance(accountNumber)).isEqualTo(30d);
		assertThat(leftService.getBalance(accountNumber)).isEqualTo(30d);
	}

	private void merge() {
		Set<Event> merged = new HashSet<>();
		merged.addAll(leftEventBus.getEvents());
		merged.addAll(rightEventBus.getEvents());

		leftEventBus.importEvents(merged);
		rightEventBus.importEvents(merged);
	}
}
