package com.paas.microservices;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

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
		UUID createdId = repo.create(requestEvent);
		assertThat(repo.getAccountsSnapshot().size()).isEqualTo(1);
	}

	@Test
	public void writtenAccountsAreReadable() {
		UUID accountNumber = UUID.randomUUID();
		Account account = new Account(accountNumber, 123.12d);
		Account returned = repo.save(new AccountUpdateRequestEvent(UUID.randomUUID(), account));
		assertThat(returned).isEqualTo(account);

		assertThat(repo.getAccountsSnapshot().size()).isEqualTo(1);

		Account actual = repo.load(accountNumber);
		assertThat(actual).isEqualTo(account);
	}

	@Test
	public void reimportingEventsDoesNotCreateDuplicates() {
		InMemoryAccountRepository left;
		left = new InMemoryAccountRepository();

		assertThat(left.getEvents().size()).isEqualTo(0);

		AccountCreateRequestEvent requestEvent = new AccountCreateRequestEvent(UUID.randomUUID(), 0d);
		UUID createdAccountNumber = left.create(requestEvent);
		assertThat(left.getEvents().size()).isEqualTo(2);

		Set<Event> leftEvents = left.getEvents();
		assertThat(leftEvents).contains(requestEvent);
		assertThat(leftEvents.stream()
				.filter(e -> e instanceof AccountCreatedEvent)
				.map(e -> (AccountCreatedEvent) e)
				.filter((e) -> e.parentTransactionId.equals(requestEvent.transactionId))
				.filter((e) -> createdAccountNumber.equals(e.createdId))
				.collect(Collectors.toList()).size()).isEqualTo(1);

		left.importEvents(leftEvents);
		Set<Event> leftEventsAfterImport = left.getEvents();
		assertThat(leftEventsAfterImport).contains(requestEvent);
		assertThat(leftEventsAfterImport.stream()
				.filter(e -> e instanceof AccountCreatedEvent)
				.map(e -> (AccountCreatedEvent) e)
				.filter((e) -> e.parentTransactionId.equals(requestEvent.transactionId))
				.filter((e) -> createdAccountNumber.equals(e.createdId))
				.collect(Collectors.toList()).size()).isEqualTo(1);
	}

	@Test
	public void mergingWorksWithoutDuplicates() {
		InMemoryAccountRepository left, right;
		left = new InMemoryAccountRepository();
		right = new InMemoryAccountRepository();

		assertCreateRequestResultsInTwoEvents(left);
		assertCreateRequestResultsInTwoEvents(right);

		Set<Event> leftEvents = left.getEvents();
		Set<Event> rightEvents = right.getEvents();
		Set<Event> mergedEvents = new LinkedHashSet<>();
		mergedEvents.addAll(leftEvents);
		mergedEvents.addAll(rightEvents);
		left.importEvents(mergedEvents);
		right.importEvents(mergedEvents);
		assertThat(left.getEvents()).isEqualTo(mergedEvents);
		assertThat(right.getEvents()).isEqualTo(mergedEvents);
	}

	@Test
	public void mergingWorksWithDuplicates() {
		InMemoryAccountRepository left, right;
		left = new InMemoryAccountRepository();
		right = new InMemoryAccountRepository();

		AccountCreateRequestEvent requestEvent = new AccountCreateRequestEvent(UUID.randomUUID(), 0d);

		left.create(requestEvent);
		right.create(requestEvent);

		Set<Event> leftEvents = left.getEvents();
		Set<Event> rightEvents = right.getEvents();
		Set<Event> mergedEvents = new LinkedHashSet<>();
		mergedEvents.addAll(leftEvents);
		mergedEvents.addAll(rightEvents);
		assertThat(mergedEvents.size()).isEqualTo(2);
	}

	private void assertCreateRequestResultsInTwoEvents(InMemoryAccountRepository repo) {
		AccountCreateRequestEvent requestEvent = new AccountCreateRequestEvent(UUID.randomUUID(), 0d);
		UUID createdAccountNumber = repo.create(requestEvent);
		assertThat(repo.getEvents().size()).isEqualTo(2);

		Set<Event> leftEvents = repo.getEvents();
		assertThat(leftEvents).contains(requestEvent);
		assertThat(leftEvents.stream()
				.filter(e -> e instanceof AccountCreatedEvent)
				.map(e -> (AccountCreatedEvent) e)
				.filter((e) -> e.parentTransactionId.equals(requestEvent.transactionId))
				.filter((e) -> createdAccountNumber.equals(e.createdId))
				.collect(Collectors.toList()).size()).isEqualTo(1);
	}
}
