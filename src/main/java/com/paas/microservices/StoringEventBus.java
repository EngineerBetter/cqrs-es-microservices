package com.paas.microservices;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.jgrapht.experimental.dag.DirectedAcyclicGraph;
import org.jgrapht.experimental.dag.DirectedAcyclicGraph.CycleFoundException;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.traverse.TopologicalOrderIterator;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.SubscriberExceptionContext;
import com.google.common.eventbus.SubscriberExceptionHandler;

public class StoringEventBus implements SubscriberExceptionHandler {
	private final EventBus eventBus;
	private final Set<Event> seenEvents;
	private final Set<Throwable> exceptionsThrown;
	private final Set<Event> failedEvents;

	public StoringEventBus() {
		this.eventBus = new EventBus(this);
		this.seenEvents = new LinkedHashSet<>();
		this.exceptionsThrown = new HashSet<>();
		this.failedEvents = new HashSet<>();
	}

	@Override
	public void handleException(Throwable exception, SubscriberExceptionContext context) {
		exceptionsThrown.add(exception);
		Object event = context.getEvent();
		if(event instanceof Event) {
			failedEvents.add((Event) event);
		}
	}

	public boolean exceptionThrownMatching(Throwable exception) {
		long count = exceptionsThrown.stream().filter((s) -> s.getClass().equals(exception.getClass()))
				.filter((e) -> e.getMessage().equals(exception.getMessage())).count();
		return count > 0;
	}

	public void post(Event event) {
		if (!seenEvents.contains(event)) {
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
		return Collections.unmodifiableSet(new LinkedHashSet<>(seenEvents));
	}

	public void importEvents(Set<Event> otherEvents) {
		eventBus.post(new ResetStateEvent());
		seenEvents.clear();

		DirectedAcyclicGraph<Event, DefaultEdge> dag = new DirectedAcyclicGraph<>(DefaultEdge.class);

		for(Event event : otherEvents) {
			dag.addVertex(event);
		}

		for(Event event : otherEvents) {
			if(event instanceof CausableEvent) {
				CausableEvent causable = (CausableEvent) event;
				try {
					dag.addDagEdge(causable.getCause(), causable);
				} catch (CycleFoundException e) {
					throw new RuntimeException("Cycle found between events");
				}
			}
		}

		TopologicalOrderIterator<Event,DefaultEdge> iterator = new TopologicalOrderIterator<>(dag);

		while(iterator.hasNext()) {
			Event event = iterator.next();
			boolean shouldSkip = false;

			if(event instanceof CausableEvent) {
				CausableEvent causable = (CausableEvent) event;

				if(failedEvents.contains(causable.getCause())) {
					shouldSkip = true;
				}
			}

			if(!shouldSkip) {
				post(event);
			} else {
				failedEvents.add(event);
			}
		}
	}
}