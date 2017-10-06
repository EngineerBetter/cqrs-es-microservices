package com.paas.microservices;

public class NullEvent implements Event {
	public NullEvent() {
	}

	@Override
	public int hashCode() {
		return 1;
	}

	@Override
	public boolean equals(Object obj) {
		return getClass().equals(obj.getClass());
	}
}
