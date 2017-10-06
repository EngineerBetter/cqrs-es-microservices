package com.paas.microservices;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jgrapht.experimental.dag.DirectedAcyclicGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.traverse.DepthFirstIterator;
import org.junit.Test;

public class GraphTest {
	@Test
	public void causableEventsAreTSortable() throws Exception {
		TestEvent createAccountReq = new TestEvent("CreateAccountReq", null);
		TestEvent accountCreated = new TestEvent("Account Created", createAccountReq);

		TestEvent eventA1 = new TestEvent("Acton CR 1", accountCreated);
		TestEvent eventA2 = new TestEvent("Acton CR 2", eventA1);
		TestEvent eventA3 = new TestEvent("Acton CR 3", eventA2);

		TestEvent eventO1 = new TestEvent("Oakhill CR 1", accountCreated);
		TestEvent eventO2 = new TestEvent("Oakhill CR 2", eventO1);
		TestEvent eventO3 = new TestEvent("Oakhill CR 3", eventO2);

		Set<TestEvent> events = new HashSet<>();
		events.add(accountCreated);
		events.add(eventA1);
		events.add(eventA2);
		events.add(eventO2);
		events.add(eventA3);
		events.add(eventO1);

		events.add(eventO3);

		DirectedAcyclicGraph<TestEvent, DefaultEdge> dag = new DirectedAcyclicGraph<>(DefaultEdge.class);
		dag.addVertex(createAccountReq);

		for(TestEvent event : events) {
			dag.addVertex(event);
		}

		for(TestEvent event : events) {
			dag.addDagEdge((TestEvent) event.getCause(), event);
		}

		List<TestEvent> ordered = new ArrayList<>();
		List<TestEvent> expected = Arrays.asList(createAccountReq, accountCreated, eventA1, eventA2, eventA3, eventO1, eventO2, eventO3);

		DepthFirstIterator<TestEvent, DefaultEdge> iterator = new DepthFirstIterator<>(dag);

		assertThat(dag.edgesOf(createAccountReq).size()).isEqualTo(1);

		while(iterator.hasNext()) {
			TestEvent event = iterator.next();
			ordered.add(event);
		}

		assertThat(ordered).isEqualTo(expected);
	}


	public static class TestEvent implements CausableEvent {
		public final String thing;
		public final Event cause;

		public TestEvent(String thing, Event cause) {
			this.thing = thing;
			this.cause = cause;
		}

		@Override
		public Event getCause() {
			return cause;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((cause == null) ? 0 : cause.hashCode());
			result = prime * result + ((thing == null) ? 0 : thing.hashCode());
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
			if (cause == null) {
				if (other.cause != null)
					return false;
			} else if (!cause.equals(other.cause))
				return false;
			if (thing == null) {
				if (other.thing != null)
					return false;
			} else if (!thing.equals(other.thing))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "TestEvent [thing=" + thing + ", cause=" + cause + "]";
		}
	}
}
