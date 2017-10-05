package com.paas.microservices;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import com.google.common.eventbus.EventBus;

public class StoringEventBus {
	private final EventBus eventBus;
	private final Set<Event> seenEvents;

	public StoringEventBus() {
		this.eventBus = new EventBus();
		this.seenEvents = new LinkedHashSet<>();
	}

	public void post(Event event) {
		if(! seenEvents.contains(event)) {
			seenEvents.add(event);
			eventBus.post(event);
		}
	}

	public void register(Object object) {
		eventBus.register(object);
	}

	public void unregister(Object object) {
		eventBus.unregister(object);
	}

	public Set<Event> getEvents() {
		return Collections.unmodifiableSet(seenEvents);
	}

	public void importEvents(Set<Event> otherEvents) {
        seenEvents.addAll(otherEvents);
	}
}