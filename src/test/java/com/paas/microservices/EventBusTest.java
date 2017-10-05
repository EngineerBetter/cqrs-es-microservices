package com.paas.microservices;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.junit.Test;

import com.google.common.eventbus.Subscribe;

public class EventBusTest {
	@Test
	public void howDoesItWork() {
		StoringEventBus bus = new StoringEventBus();
		bus.register(new TestHandler(bus));
		AcknowledgementHandler ackHandler = new AcknowledgementHandler();
		bus.register(ackHandler);
		UUID eventId = UUID.randomUUID();
		bus.post(new TestEvent(eventId, "some value"));
		bus.post(new TestEvent(eventId, "some value"));
		bus.post(new TestEvent(UUID.randomUUID(), "some other value"));

		assertThat(ackHandler.wasAcknowledged("some value")).isTrue();
		assertThat(ackHandler.wasAcknowledged("some other value")).isTrue();
		assertThat(ackHandler.handledEvents).isEqualTo(2);
	}

	public static class TestHandler {
		private final StoringEventBus bus;

		public TestHandler(StoringEventBus bus) {
			this.bus = bus;
		}

		@Subscribe
		public void handle(TestEvent event) {
			System.out.println(event);
			bus.post(new AcknowledgedEvent(event.foo));
		}
	}

	public static class TestEvent implements Event {
		public final UUID eventId;
		public final String foo;

		public TestEvent(UUID eventId, String foo) {
			this.eventId = eventId;
			this.foo = foo;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((foo == null) ? 0 : foo.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			TestEvent other = (TestEvent) obj;
			if (foo == null) {
				if (other.foo != null)
					return false;
			} else if (!foo.equals(other.foo))
				return false;
			return true;
		}
		@Override
		public String toString() {
			return "TestEvent [foo=" + foo + "]";
		}
	}

	public static class AcknowledgementHandler {
		private Set<String> handledStrings;
		private int handledEvents;

		public AcknowledgementHandler() {
			handledStrings = new HashSet<>();
		}

		@Subscribe
		public void handle(AcknowledgedEvent event) {
			handledStrings.add(event.acknowledgedString);
			handledEvents++;
		}

		public int handledEventCount() {
			return handledEvents;
		}

		public boolean wasAcknowledged(String s) {
			return handledStrings.contains(s);
		}
	}

	public static class AcknowledgedEvent implements Event {
		public final String acknowledgedString;

		public AcknowledgedEvent(String acknowledgedString) {
			super();
			this.acknowledgedString = acknowledgedString;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((acknowledgedString == null) ? 0 : acknowledgedString.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			AcknowledgedEvent other = (AcknowledgedEvent) obj;
			if (acknowledgedString == null) {
				if (other.acknowledgedString != null)
					return false;
			} else if (!acknowledgedString.equals(other.acknowledgedString))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "AcknowledgedEvent [acknowledgedString=" + acknowledgedString + "]";
		}
	}
}
