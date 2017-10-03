package com.paas.microservices;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.LinkedHashMap;
import java.util.Map;
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

	@Test
	public void reimportingEventsDoesNotCreateDuplicates() {
		InMemoryAccountRepository left;
		left = new InMemoryAccountRepository();

		assertThat(left.getEvents().size()).isEqualTo(0);

		AccountCreateRequestEvent requestEvent = new AccountCreateRequestEvent(UUID.randomUUID(), 0d);
		Integer createdAccountNumber = left.create(requestEvent);
		assertThat(left.getEvents().size()).isEqualTo(2);

		Map<UUID, Event> leftEvents = left.getEvents();
		assertThat(leftEvents).containsEntry(requestEvent.transactionId, requestEvent);
		assertThat(leftEvents.values().stream()
				.filter(e -> e instanceof AccountCreatedEvent)
				.map(e -> (AccountCreatedEvent) e)
				.filter((e) -> e.parentTransactionId.equals(requestEvent.transactionId))
				.filter((e) -> createdAccountNumber.equals(e.createdId))
				.collect(Collectors.toList()).size()).isEqualTo(1);

		left.importEvents(leftEvents);
		Map<UUID, Event> leftEventsAfterImport = left.getEvents();
		assertThat(leftEventsAfterImport).containsEntry(requestEvent.transactionId, requestEvent);
		assertThat(leftEventsAfterImport.values().stream()
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

		Map<UUID, Event> leftEvents = left.getEvents();
		Map<UUID, Event> rightEvents = right.getEvents();
		Map<UUID, Event> mergedEvents = new LinkedHashMap<>();
		mergedEvents.putAll(leftEvents);
		mergedEvents.putAll(rightEvents);
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

		Map<UUID, Event> leftEvents = left.getEvents();
		Map<UUID, Event> rightEvents = right.getEvents();
		Map<UUID, Event> mergedEvents = new LinkedHashMap<>();
		mergedEvents.putAll(leftEvents);
		mergedEvents.putAll(rightEvents);
		assertThat(mergedEvents.size()).isEqualTo(2);
	}

	private void assertCreateRequestResultsInTwoEvents(InMemoryAccountRepository repo) {
		AccountCreateRequestEvent requestEvent = new AccountCreateRequestEvent(UUID.randomUUID(), 0d);
		Integer createdAccountNumber = repo.create(requestEvent);
		assertThat(repo.getEvents().size()).isEqualTo(2);

		Map<UUID, Event> leftEvents = repo.getEvents();
		assertThat(leftEvents).containsEntry(requestEvent.transactionId, requestEvent);
		assertThat(leftEvents.values().stream()
				.filter(e -> e instanceof AccountCreatedEvent)
				.map(e -> (AccountCreatedEvent) e)
				.filter((e) -> e.parentTransactionId.equals(requestEvent.transactionId))
				.filter((e) -> createdAccountNumber.equals(e.createdId))
				.collect(Collectors.toList()).size()).isEqualTo(1);
	}
}
