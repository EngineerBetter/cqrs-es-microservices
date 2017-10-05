
package com.paas.microservices;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public class EventStore {
	private final Set<Event> events;

	public EventStore() {
		events = new LinkedHashSet<>();
	}

	public boolean isNewEvent(Event event) {
		return ! events.contains(event);
	}

	public void importEvents(Set<Event> other) {
		events.addAll(other);
	}

	public void add(Event event) {
		events.add(event);
	}

	public Set<Event> getEvents() {
		return Collections.unmodifiableSet(events);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((events == null) ? 0 : events.hashCode());
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
		EventStore other = (EventStore) obj;
		if (events == null) {
			if (other.events != null)
				return false;
		} else if (!events.equals(other.events))
			return false;
		return true;
	}
}
