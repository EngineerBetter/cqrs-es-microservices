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
		repo = new InMemoryAccountRepository(new StoringEventBus());
	}

	@Test
	public void accountsAreCreatable() {
		assertThat(repo.getTotalNumberOfAccounts()).isEqualTo(0);
		AccountCreateRequestEvent requestEvent = new AccountCreateRequestEvent(UUID.randomUUID(), 0d);
		repo.create(requestEvent);
		assertThat(repo.getTotalNumberOfAccounts()).isEqualTo(1);
	}

	@Test
	public void writtenAccountsAreReadable() {
		UUID accountNumber = UUID.randomUUID();
		Account account = new Account(accountNumber, 123.12d);
		Account returned = repo.save(new AccountUpdateRequestEvent(UUID.randomUUID(), account));
		assertThat(returned).isEqualTo(account);

		assertThat(repo.getTotalNumberOfAccounts()).isEqualTo(1);

		Account actual = repo.load(accountNumber);
		assertThat(actual).isEqualTo(account);
	}

	@Test
	public void reimportingEventsDoesNotCreateDuplicates() {
		InMemoryAccountRepository left;
		StoringEventBus leftEventBus = new StoringEventBus();
		left = new InMemoryAccountRepository(leftEventBus);

		assertThat(leftEventBus.getEvents().size()).isEqualTo(0);

		UUID createdAccountNumber = UUID.randomUUID();
		AccountCreateRequestEvent requestEvent = new AccountCreateRequestEvent(createdAccountNumber, 0d);
		leftEventBus.post(requestEvent);
		assertThat(leftEventBus.getEvents().size()).isEqualTo(2);

		Set<Event> leftEvents = leftEventBus.getEvents();
		assertThat(leftEvents).contains(requestEvent);
		assertThat(leftEvents.stream()
				.filter(e -> e instanceof AccountCreatedEvent)
				.map(e -> (AccountCreatedEvent) e)
				.filter((e) -> e.parentEventId.equals(requestEvent.eventId))
				.filter((e) -> createdAccountNumber.equals(e.createdId))
				.collect(Collectors.toList()).size()).isEqualTo(1);


		leftEventBus.importEvents(leftEvents);
		Set<Event> leftEventsAfterImport = leftEventBus.getEvents();
		assertThat(leftEventsAfterImport).contains(requestEvent);
		assertThat(leftEventsAfterImport.stream()
				.filter(e -> e instanceof AccountCreatedEvent)
				.map(e -> (AccountCreatedEvent) e)
				.filter((e) -> e.parentEventId.equals(requestEvent.eventId))
				.filter((e) -> createdAccountNumber.equals(e.createdId))
				.collect(Collectors.toList()).size()).isEqualTo(1);
	}

	@Test
	public void mergingWorksWithoutDuplicates() {
		InMemoryAccountRepository left, right;
		StoringEventBus leftEventBus = new StoringEventBus();
		StoringEventBus rightEventBus = new StoringEventBus();
		left = new InMemoryAccountRepository(leftEventBus);
		right = new InMemoryAccountRepository(rightEventBus);

		assertCreateRequestResultsInTwoEvents(left, leftEventBus);
		assertThat(left.getTotalNumberOfAccounts()).isEqualTo(1);
		assertCreateRequestResultsInTwoEvents(right, rightEventBus);
		assertThat(right.getTotalNumberOfAccounts()).isEqualTo(1);


		Set<Event> leftEvents = leftEventBus.getEvents();
		Set<Event> rightEvents = rightEventBus.getEvents();
		Set<Event> mergedEvents = new LinkedHashSet<>();
		mergedEvents.addAll(leftEvents);
		mergedEvents.addAll(rightEvents);
		leftEventBus.importEvents(mergedEvents);
		rightEventBus.importEvents(mergedEvents);
		assertThat(leftEventBus.getEvents()).isEqualTo(mergedEvents);
		assertThat(rightEventBus.getEvents()).isEqualTo(mergedEvents);
		assertThat(left.getTotalNumberOfAccounts()).isEqualTo(2);
		assertThat(right.getTotalNumberOfAccounts()).isEqualTo(2);
	}

	@Test
	public void mergingWorksWithDuplicates() {
		InMemoryAccountRepository left, right;
		StoringEventBus leftEventBus = new StoringEventBus();
		StoringEventBus rightEventBus = new StoringEventBus();
		left = new InMemoryAccountRepository(leftEventBus);
		right = new InMemoryAccountRepository(rightEventBus);

		AccountCreateRequestEvent requestEvent = new AccountCreateRequestEvent(UUID.randomUUID(), 0d);

		leftEventBus.post(requestEvent);
		rightEventBus.post(requestEvent);

		Set<Event> leftEvents = leftEventBus.getEvents();
		Set<Event> rightEvents = rightEventBus.getEvents();
		Set<Event> mergedEvents = new LinkedHashSet<>();
		mergedEvents.addAll(leftEvents);
		mergedEvents.addAll(rightEvents);
		assertThat(mergedEvents.size()).isEqualTo(2);
	}

	private void assertCreateRequestResultsInTwoEvents(InMemoryAccountRepository repo, StoringEventBus eventBus) {
		UUID createdAccountNumber = UUID.randomUUID();
		AccountCreateRequestEvent requestEvent = new AccountCreateRequestEvent(createdAccountNumber, 0d);
		eventBus.post(requestEvent);
		assertThat(eventBus.getEvents().size()).isEqualTo(2);

		Set<Event> leftEvents = eventBus.getEvents();
		assertThat(leftEvents).contains(requestEvent);
		assertThat(leftEvents.stream()
				.filter(e -> e instanceof AccountCreatedEvent)
				.map(e -> (AccountCreatedEvent) e)
				.filter((e) -> e.parentEventId.equals(requestEvent.eventId))
				.filter((e) -> createdAccountNumber.equals(e.createdId))
				.collect(Collectors.toList()).size()).isEqualTo(1);
	}
}
